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
                            AND LOWER(club)=LOWER('#{params[:id]}')")
                    .order(:lastname)
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
                                AND LOWER(club) LIKE LOWER('%#{club}%')")
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
    player_ranking = {}
    u12_players = []
    u14_players = []
    u16_players = []
    u18_players = []
    players.each do |player|
      ranking = Ranking.find_by(dtb_id: player.dtb_id,
                                age_group: 'Overall',
                                date: current_quarter)
      player_data = { dtb_id: player.dtb_id, lastname: player.lastname,
                      firstname: player.firstname, score: ranking.score,
                      rank: ranking.ranking_position }
      # find age group ranking for player and push to group
      age_group = Ranking.find_by(dtb_id: player.dtb_id, date: current_quarter,
                                  yob_ranking: false, age_group_ranking: true,
                                  year_end_ranking: false)
                         .age_group
      case age_group
      when 'U12'
        u12_players.push(player_data)
      when 'U14'
        u14_players.push(player_data)
      when 'U16'
        u16_players.push(player_data)
      when 'U18'
        u18_players.push(player_data)
      end
    end
    player_ranking['U12'] = u12_players unless u12_players.empty?
    player_ranking['U14'] = u14_players unless u14_players.empty?
    player_ranking['U16'] = u16_players unless u16_players.empty?
    player_ranking['U18'] = u18_players unless u18_players.empty?

    player_ranking
  end
end
