class PlayersController < ApplicationController
  def index
    # TODO: Search and list
  end

  def show
    @player = Player.find_by_dtb_id(params[:id])
  end
end
