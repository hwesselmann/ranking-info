class PlayersController < ApplicationController
  def show
    @player = Player.find_by_dtb_id(params[:id])
  end
end
