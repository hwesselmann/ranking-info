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
    rescue
      redirect_to players_path, flash: { danger: 'Der Spieler wurde leider nicht gefunden' }
    end
  end

  private

  def get_current_rankings(dtb_id)
    # TODO: too many queries - try something better later on, e.g. a view in the db
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
          if Ranking.select(:date).order(date: :desc).distinct.size > 1
            previous_quarter = Ranking.select(:date).order(date: :desc).distinct[1].date
            # 5. calculate differences
          end
          rankings.push(ranking)
        end
      end
    end
    rankings
  end

  def try_to_find_current_age_group(dtb_id)
    current_year = Time.now.year
    current_age_group = 0
    [11, 12, 13, 14, 15, 16, 17, 18].each do |ag|
      if (current_year - ag.to_i).eql?(dtb_id.to_s[1, 2].to_i)
        current_age_group = ag
      end
    end
    current_age_group
  end
end
