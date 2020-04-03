class PlayersController < ApplicationController
  def index
    if params[:dtb_id] && !params[:dtb_id].eql?('')
      @players = Player.where(dtb_id: params[:dtb_id])
      # should return exactly one match => redirect to profile
      redirect_to action: 'show', id: params[:dtb_id] if @players.size == 1
    elsif params[:lastname] && !params[:lastname].eql?('')
      # fuzzy!
      if params[:yob] && !params[:yob].eql?('')
        # fuzzy lastname in yob
        yob_male = params[:yob][2, 4].to_i + 100
        yob_female = yob_male + 100
        @players = Player.where("lastname LIKE '%#{params[:lastname]}%' AND (dtb_id LIKE '#{yob_male}%' OR dtb_id LIKE '#{yob_female}%')")
      else
        @players = Player.where("lastname LIKE '%#{params[:lastname]}%'")
      end
    elsif params[:yob] && !params[:yob].eql?('')
      yob_male = params[:yob][2, 4].to_i + 100
      yob_female = yob_male + 100
      @players = Player.where("dtb_id LIKE '#{yob_male}%' OR dtb_id LIKE '#{yob_female}%'")
    elsif params[:commit]
      # search was fired without parameters => show all
      @players = Player.all
    end
  end

  def show
    @player = Player.find_by_dtb_id(params[:id])
  end
end
