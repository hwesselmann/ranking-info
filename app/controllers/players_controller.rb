class PlayersController < ApplicationController
  def index
    if params[:dtb_id] && !params[:dtb_id].eql?('')
      @players = Player.where("dtb_id LIKE '#{params[:dtb_id]}%'")
      # should return exactly one match => redirect to profile
      redirect_to action: 'show', id: params[:dtb_id] if @players.size == 1
    elsif params[:lastname] && !params[:lastname].eql?('')
      # fuzzy!
      if params[:yob] && !params[:yob].eql?('')
        # fuzzy lastname in yob
        yob_male = params[:yob][2, 4].to_i + 100
        yob_female = yob_male + 100
        @players = Player.where("lastname LIKE '%#{params[:lastname]}%' AND (dtb_id LIKE '#{yob_male}%' OR dtb_id LIKE '#{yob_female}%')").order(:lastname, :dtb_id)
      else
        @players = Player.where("lastname LIKE '%#{params[:lastname]}%'").order(:lastname, :dtb_id)
      end
      redirect_to action: 'show', id: @players[0].dtb_id if @players.size == 1
    elsif params[:yob] && !params[:yob].eql?('')
      yob_male = params[:yob][2, 4].to_i + 100
      yob_female = yob_male + 100
      @players = Player.where("dtb_id LIKE '#{yob_male}%' OR dtb_id LIKE '#{yob_female}%'").order(:lastname, :dtb_id)
      redirect_to action: 'show', id: @players[0].dtb_id if @players.size == 1
    elsif params[:commit]
      # search was fired without parameters => show all
      @players = Player.all.order(:lastname, :dtb_id)
    end
  end

  def show
    begin
      @player = Player.find_by_dtb_id(params[:id])
      @available_quarters = helpers.fetch_available_quarters(dtb_id: @player.dtb_id)
      @current_rankings = get_current_rankings(@player.dtb_id)
      @complete_rankings = get_complete_rankings(@player.dtb_id).reverse!
      @data_for_last_twelve_months = data_for_last_twelve_months(@player.dtb_id)
      @data_diagram_complete = data_diagram_complete(@player.dtb_id)
    rescue
      redirect_to players_path, flash: { danger: 'Der Spieler wurde leider nicht gefunden' }
    end
  end

  private

  def get_current_rankings(dtb_id)
    rankings = []
    # 1. get the latest available period for player. If nothing is available => message
    current_quarter = Ranking.select(:date).order(date: :desc).distinct.first.date
    # 2. get rankings for that period
    current_rankings = Ranking.where(dtb_id: dtb_id, date: current_quarter, yob_ranking: false, age_group_ranking: false, year_end_ranking: false).order(:age_group)
    if current_rankings.size.positive?
      current_rankings.each do |current_ranking|
        ranking = {}
        # 3. fill the initial hash
        unless current_ranking.age_group.eql?('Overall')
          ranking['age_group'] = current_ranking.age_group
          ranking['position'] = current_ranking.ranking_position
          ranking['score'] = current_ranking.score
          # 4. get rankings of period - 1
          if Ranking.select(:date).where(dtb_id: dtb_id).order(date: :desc).distinct.size > 1
            previous_quarter = Ranking.select(:date).order(date: :desc).distinct[1].date
            # 5. calculate differences
            prev_ranking = Ranking.find_by(dtb_id: dtb_id, age_group: current_ranking.age_group, date: previous_quarter, yob_ranking: false, age_group_ranking: false, year_end_ranking: false)
            position_change = prev_ranking.ranking_position - current_ranking.ranking_position
            ranking['position_change'] = if position_change.positive? then "+#{position_change}"
                                         else position_change.to_s
                                         end
            score_change = current_ranking.score.to_f - prev_ranking.score.to_f
            ranking['score_change'] = if score_change.positive? then "+#{score_change}"
                                      else score_change.to_s
                                      end
          end
          rankings.push(ranking)
        end
      end
    end
    rankings
  end

  def get_complete_rankings(dtb_id)
    db_rankings = Ranking.where(dtb_id: dtb_id, yob_ranking: false, age_group_ranking: false, year_end_ranking: false).order(:date, :age_group)
    rankings = []
    ranking = {}
    start_year = 0
    current_quarter = ''
    db_rankings.each do |ran|
      case ran.date.month
      when 1
        quarter = 'Q1'
      when 4
        quarter = 'Q2'
      when 7
        quarter = 'Q3'
      when 10
        quarter = 'Q4'
      end
      if start_year.eql?(ran.date.year)
        if current_quarter.eql?(quarter)
          # same year, same quarter => add age group
          ranking[ran.age_group] = ran.ranking_position
        else
          # same year, other quarter => push and start new
          rankings.push(ranking)
          current_quarter = quarter
          ranking = {}
          ranking['year'] = ran.date.year
          ranking['quarter'] = quarter
          ranking[ran.age_group] = ran.ranking_position
          ranking['score'] = ran.score
        end
      else
        # different year => check if first run and start new
        rankings.push(ranking) unless start_year.eql?(0)
        start_year = ran.date.year
        current_quarter = quarter
        ranking = {}
        ranking['year'] = ran.date.year
        ranking['quarter'] = quarter
        ranking[ran.age_group] = ran.ranking_position
        ranking['score'] = ran.score
      end
    end
    rankings.push(ranking)
    rankings
  end

  def data_for_last_twelve_months(dtb_id)
    rankings = Ranking.where(dtb_id: dtb_id, yob_ranking: false,
                             age_group_ranking: true, year_end_ranking: false)
                      .order(date: :desc, age_group: :asc)
                      .limit(4)
    collect_diagram_data(rankings)
  end

  def data_diagram_complete(dtb_id)
    rankings = Ranking.where(dtb_id: dtb_id, yob_ranking: false,
                             age_group_ranking: false, year_end_ranking: false)
                      .order(:date, :age_group)
    collect_diagram_data(rankings)
  end

  def collect_diagram_data(rankings)
    scores = {}
    u12_positions = {}
    u14_positions = {}
    u16_positions = {}
    u18_positions = {}

    rankings.reverse_each do |ranking|
      period = (ranking.date - 1.day).strftime('%d.%m.%Y')
      scores[period] = ranking.score
      case ranking.age_group
      when 'U12'
        u12_positions[period] = ranking.ranking_position
      when 'U14'
        u14_positions[period] = ranking.ranking_position
      when 'U16'
        u16_positions[period] = ranking.ranking_position
      when 'U18'
        u18_positions[period] = ranking.ranking_position
      end
    end

    diagram_data = [{ name: 'Punkte', data: scores }]

    diagram_data.push({ name: 'U12', data: u12_positions }) if u12_positions.size.positive?
    diagram_data.push({ name: 'U14', data: u14_positions }) if u14_positions.size.positive?
    diagram_data.push({ name: 'U16', data: u16_positions }) if u16_positions.size.positive?
    diagram_data.push({ name: 'U18', data: u18_positions }) if u18_positions.size.positive?

    diagram_data
  end
end
