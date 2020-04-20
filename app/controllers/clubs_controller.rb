# frozen_string_literal: true

#
# Controller for all actions concerning club data.
#
class ClubsController < ApplicationController
  def index
    clubs = []
    if params[:commit]
      current_quarter = Ranking.select(:date).order(date: :desc)
                               .distinct.first.date
      player_clubs = Player.select(:club)
                           .where("dtb_id IN (SELECT DISTINCT(dtb_id) FROM rankings 
                                   WHERE date='#{current_quarter}') 
                                   AND club LIKE '%#{params[:club]}%'")
                           .order(:club)
                           .distinct
      player_clubs.each do |c|
        player_count = Player.select(:club)
                             .where("dtb_id IN (SELECT DISTINCT(dtb_id)
                                     FROM rankings 
                                     WHERE date='#{current_quarter}')
                                     AND club='#{c.club}'").count
        clubs.push({ name: c.club, player_count: player_count })
      end
    end
    @clubs = clubs
  end

  def show
    current_quarter = Ranking.select(:date).order(date: :desc)
                             .distinct.first.date
    players = Player.where("dtb_id IN (SELECT DISTINCT(dtb_id) FROM rankings
                            WHERE date='#{current_quarter}')
                            AND club='#{params[:id]}'").order(:lastname)
    player_ranking = []
    players.each do |player|
      ranking = Ranking.find_by(dtb_id: player.dtb_id,
                                age_group: 'Overall',
                                date: current_quarter)
      player_ranking.push({ dtb_id: player.dtb_id, lastname: player.lastname,
                            firstname: player.firstname, score: ranking.score,
                            rank: ranking.ranking_position })
    end
    @players = player_ranking
  end
end
