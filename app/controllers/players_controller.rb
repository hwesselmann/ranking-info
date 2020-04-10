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
    # 1. get current real age group

    # 2. get the latest available period for player

    # 3. get rankings for that period

    # 4. get rankings for period - 1

    # 5. calculate differences

    # 6. pass to view
  end
end
