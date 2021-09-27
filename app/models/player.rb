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

  def self.find_all_players
    players_to_filter = Ranking.select(:dtb_id, :lastname, :firstname, :federation, :club, :nationality, :date)
                               .order(:lastname, :dtb_id, date: :desc).distinct
    players = []
    last_dtb_id = 0
    players_to_filter.each do |player|
      next if last_dtb_id.eql?(player.dtb_id)

      players.push(player)
      last_dtb_id = player.dtb_id
    end
    players
  end

  def self.find_players_by_lastname(lastname)
    players_to_filter = Ranking.select(:dtb_id, :lastname, :firstname, :club, :federation, :nationality, :date)
                               .where("LOWER(lastname) LIKE LOWER('%#{lastname}%')")
                               .order(:lastname, :dtb_id, date: :desc)
                               .distinct
    players = []
    last_dtb_id = 0
    players_to_filter.each do |player|
      next if last_dtb_id.eql?(player.dtb_id)

      players.push(player)
      last_dtb_id = player.dtb_id
    end
    players
  end

  def self.find_players_by_lastname_and_yob(lastname, yob_male, yob_female)
    players_to_filter = Ranking.select(:dtb_id, :lastname, :firstname, :federation, :club, :nationality)
                               .where("LOWER(lastname) LIKE LOWER('%#{lastname}%')
                                       AND ((dtb_id >= #{yob_male * 100_000}
                                       AND dtb_id <= #{yob_male * 100_000 + 99_999})
                                       OR (dtb_id>= #{yob_female * 100_000}
                                       AND dtb_id <= #{yob_female * 100_000 + 99_999}))")
                               .order(:lastname, :dtb_id, date: :desc)
                               .distinct
    players = []
    last_dtb_id = 0
    players_to_filter.each do |player|
      next if last_dtb_id.eql?(player.dtb_id)

      players.push(player)
      last_dtb_id = player.dtb_id
    end
    players
  end

  def self.find_players_by_dtb_id(dtb_id_start, dtb_id_end)
    players_to_filter = Ranking.select(:dtb_id, :lastname, :firstname, :federation, :club, :nationality)
                               .where("dtb_id >= #{dtb_id_start}
                                       AND dtb_id <= #{dtb_id_end}")
                               .order(:lastname, :dtb_id, date: :desc)
                               .distinct
    players = []
    last_dtb_id = 0
    players_to_filter.each do |player|
      next if last_dtb_id.eql?(player.dtb_id)

      players.push(player)
      last_dtb_id = player.dtb_id
    end
    players
  end
  
  def self.find_players_by_yob(yob_male, yob_female)
    players_to_filter = Ranking.select(:dtb_id, :lastname, :firstname, :federation, :club, :nationality)
                               .where("(dtb_id >= #{yob_male * 100_000}
                                       AND dtb_id <= #{yob_male * 100_000 + 99_999})
                                       OR (dtb_id >= #{yob_female * 100_000}
                                       AND dtb_id <= #{yob_female * 100_000 + 99_999})")
                               .order(:lastname, :dtb_id, date: :desc)
                               .distinct
    players = []
    last_dtb_id = 0
    players_to_filter.each do |player|
      next if last_dtb_id.eql?(player.dtb_id)

      players.push(player)
      last_dtb_id = player.dtb_id
    end
    players
  end

  attr_accessor :dtb_id, :current_ranking, :current_score, :lastname, :firstname
  attr_accessor :federation, :club, :nationality, :clubs, :rankings
end
