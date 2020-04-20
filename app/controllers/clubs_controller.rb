# frozen_string_literal: true

#
# Controller for all actions concerning club data.
#
class ClubsController < ApplicationController
  def index
    clubs = []
    if params[:commit]
      quarter = current_quarter
      player_clubs = find_all_clubs(quarter, params[:club])
      player_clubs.each do |c|
        player_count = count_players_for_club(quarter, c.club)
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
    player_ranking = fill_club_info(players)
    @players = player_ranking
  end

  private

  def current_quarter
    Ranking.select(:date)
           .order(date: :desc)
           .distinct
           .first
           .date
  end

  def find_all_clubs(quarter, club)
    Player.select(:club)
          .where("dtb_id IN (SELECT DISTINCT(dtb_id)
                                FROM rankings WHERE date='#{quarter}')
                                AND club LIKE '%#{club}%'")
          .order(:club)
          .distinct
  end

  def count_players_for_club(quarter, club)
    Player.select(:club)
          .where("dtb_id IN (SELECT DISTINCT(dtb_id) FROM rankings
                  WHERE date='#{quarter}') AND club='#{club}'")
          .count
  end

  def fill_club_info(players)
    player_ranking = []
    players.each do |player|
      ranking = Ranking.find_by(dtb_id: player.dtb_id,
                                age_group: 'Overall',
                                date: current_quarter)
      player_ranking.push({ dtb_id: player.dtb_id, lastname: player.lastname,
                            firstname: player.firstname, score: ranking.score,
                            rank: ranking.ranking_position })
    end
    player_ranking
  end
end
