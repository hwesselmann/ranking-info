# frozen_string_literal: true

#
# Model for a player on the ranking (this is a transient object)
#
class Player
  def self.load_player_profile(dtb_id)
    player = Player.new
    current_data = Ranking.where(dtb_id: dtb_id, age_group: 'overall').order(date: :desc).first
    player.dtb_id = dtb_id
    player.lastname = current_data.lastname
    player.firstname = current_data.firstname
    player.nationality = current_data.nationality
    player.club = current_data.club
    player.federation = current_data.federation
    player.clubs = player.fetch_clubs(dtb_id)
    player
  end

  def fetch_clubs(dtb_id)
    clubs = []
    all_clubs_from_rankings = Ranking.where(dtb_id: dtb_id, age_group: 'overall').order(date: :desc)
    last_club = ''
    all_clubs_from_rankings.each do |item|
      next if last_club.eql?(item.club)

      club = Club.new
      club.name = item.club
      club.federation = item.federation
      clubs.push club
      last_club = item.club
    end
    clubs
  end

  attr_accessor :dtb_id, :current_ranking, :current_score, :lastname, :firstname
  attr_accessor :federation, :club, :nationality, :clubs, :rankings
end
