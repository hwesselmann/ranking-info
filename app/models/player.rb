# frozen_string_literal: true

require 'csv'

require_relative 'import/import_player.rb'
require_relative 'import/import_ranking.rb'

#
# Model for a player on the ranking, includes importing logic
# for new rankings.
#
class Player < ApplicationRecord
  has_many :rankings, primary_key: 'dtb_id', foreign_key: 'dtb_id'
  has_many :clubs, primary_key: 'dtb_id', foreign_key: 'dtb_id'

  def self.import_rankings(file)
    period_to_import = extract_period_from_filename(file)
    players_to_import = read_from_csv(file)
    save_imported_players(players_to_import, period_to_import)
    # gender?
    gender_factor = if players_to_import.fetch(0).dtb_id[0].eql?(1) then 100
                    else 200
                    end
    calculate_rankings(period_to_import, gender_factor)
  end

  #
  # Extract the period the import file is for from the filename.
  #
  # @param [String] filename filename to extract period from
  #
  # @return [Date] Ruby Date object containing the extracted period
  #
  def self.extract_period_from_filename(filename)
    logger.debug "extracting period part from file '#{filename}'"
    filename_without_path = filename.split('/').last
    filename_without_extension = filename_without_path.split('.').first
    filename_parts = filename_without_extension.split('_')
    if filename_parts.size >= 2
      create_time_from_string(filename_parts)
    else
      logger.error "could not retrieve period part from filename '#{filename}'"
      raise "could not retrieve period part from filename '#{filename}'"
    end
  end

  #
  # Read entries from a given file and map them to player model data.
  #
  # @param [String] file input csv file
  #
  # @return [Array] array of player data sets
  #
  def self.read_from_csv(file)
    logger.debug "reading player data from file '#{file}'"
    input = CSV.read(file, encoding: 'utf-8')
    players = []
    input.each do |entry|
      player = create_import_player(entry)
      players.push(player)
      logger.info "read player data: #{player.firstname} #{player.lastname}"
    end
    players
  end

  def self.create_import_player(entry)
    player = ImportPlayer.new
    player.current_ranking = entry[0].to_i
    player.lastname = entry[1]
    player.firstname = entry[2]
    player.nationality = entry[3]
    player.dtb_id = entry[4].split(' ').first.to_i
    player.federation = entry[4].split(' ').last
    player.club = entry[5]
    player.current_score = entry[6]
    player
  end

  #
  # Save an array of player objects for a period into a dabatase.
  #
  # @param [Array] players_to_import players to save
  # @param [Time] period_to_import period for the current data import
  #
  def self.save_imported_players(players_to_import, period_to_import)
    logger.info "Started processing save for #{players_to_import.size} players"
    count_inserted = 0
    count_updated = 0
    players_to_import.each do |player|
      existing_player = Player.where(dtb_id: player.dtb_id).first
      if existing_player.nil?
        rankings = []
        ranking = fill_ranking_for_player('Overall', period_to_import, player)
        rankings.push(ranking)
        player.rankings = rankings
        save_imported_player(player)
        count_inserted += 1
      else
        # update existing player with new data
        unless existing_player.club.eql?(player.club)
          club = Club.new(dtb_id: existing_player.dtb_id,
                          club: existing_player.club,
                          federation: existing_player.federation)
          club.save
          existing_player.club = player.club
          existing_player.federation = player.federation
        end
        ranking = Ranking.new(dtb_id: player.dtb_id,
                              age_group: 'Overall',
                              date: period_to_import,
                              ranking_position: player.current_ranking,
                              score: player.current_score,
                              age_group_ranking: false,
                              yob_ranking: false,
                              year_end_ranking: false)
        ranking.save
        existing_player.save

        count_updated += 1
      end
    end
    logger.info "Finished processing #{players_to_import.size} player entries.
                (#{count_inserted} new, #{count_updated} updated)"
  end

  #
  # Fill the ranking for a currently imported player.
  #
  # @param [String] ranking_type ranking type/age group, e.g. 'Overall', 'U12'
  # @param [Time] period_end period currently processed
  # @param [Player] player player to import
  #
  # @return [Ranking] ranking object
  #
  def self.fill_ranking_for_player(ranking_type, period_end, player)
    ranking = ImportRanking.new
    ranking.age_group = ranking_type
    ranking.date = period_end
    ranking.dtb_id = player.dtb_id
    ranking.ranking_position = player.current_ranking
    ranking.score = player.current_score
    ranking.age_group_ranking = false
    ranking.yob_ranking = false
    ranking.year_end_ranking = false

    ranking
  end

  #
  # Get a range of years of birth to retrieve for ranking calculation.
  #
  # @param [String] yob
  # @param [Integer] age_group to fetch
  # @param [Time] period to calculate
  # @param [Integer] gender factor for dtb-id prefix
  #
  # @return [Array] array containing range to fetch for calculation of rankings
  #
  def self.yob_range_to_fetch(yob, age_group, period, gender_factor)
    classes_of_players_to_retrieve = []
    yob_gender_marker = yob[2, 4].to_i + gender_factor
    i = 0
    while age_group >= period.year - yob.to_i + i
      classes_of_players_to_retrieve.push(yob_gender_marker - i)
      i += 1
    end
    classes_of_players_to_retrieve
  end

  #
  # sort the rankings of a given array of rankings for a passed in age group.
  #
  # @param [Array] rankings array of rankings to sort
  # @param [Integer] age_group age group to sort for
  # @param [Boolean] is_yob_ranking is this a ranking for a year of birth group?
  # @param [Boolean] is_age_group_ranking is this a ranking for an age goup?
  #
  # @return [Array] sorted rankings for requested age group
  #
  def self.sort_rankings_for_age_group(rankings, age_group, is_yob_ranking, is_age_group_ranking, year_end_ranking)
    sorted_rankings = []
    last_rank = 0
    start_ranking = 0
    count_up = 1
    rankings.each do |curr_ranking|
      if curr_ranking.ranking_position > last_rank
        last_rank = curr_ranking.ranking_position
        start_ranking = count_up
      end
      curr_ranking.age_group = "U#{age_group}"
      curr_ranking.yob_ranking = is_yob_ranking
      curr_ranking.age_group_ranking = is_age_group_ranking
      curr_ranking.year_end_ranking = year_end_ranking
      curr_ranking.ranking_position = start_ranking
      # ranking is only counted up for German players,
      # foreign player get the right position, but are not adding
      # up to the actual position, also players with pr do not count
      if Player.find_by(dtb_id: curr_ranking.dtb_id).nationality.eql?('GER') &&
         !curr_ranking.score.eql?('0,0')
        count_up += 1
      end
      sorted_rankings.push(curr_ranking)
    end
    sorted_rankings
  end

  #
  # Saves a single player including rankings and club history.
  # An existing player will first be removed from the database completely
  # and then be re-entered including alls past data.
  #
  # @param [ImportedPlayer] player player to save
  #
  def self.save_imported_player(imported_player)
    # save player with rankings and clubs
    player = Player.new(firstname: imported_player.firstname,
                        lastname: imported_player.lastname,
                        club: imported_player.club,
                        federation: imported_player.federation,
                        nationality: imported_player.nationality,
                        dtb_id: imported_player.dtb_id)
    player.save

    unless imported_player.rankings.nil?
      imported_player.rankings.each do |imported|
        ranking = create_ranking(imported)
        ranking.save
      end
    end

    return if imported_player.clubs.nil?

    imported_player.clubs.each do |imported_club|
      club = Club.new(club: imported_club, dtb_id: imported_player.dtb_id)
      club.save
    end
  end

  #
  # Use an input string in foramt 'yyyymmdd' to create a Time object
  #
  # @param [String] input date as String 'yyyymmdd'
  #
  # @return [Time] Ruby time object corresponding to the input
  #
  def self.create_time_from_string(input)
    year = input.last[0, 4]
    month = input.last[4, 2]
    day = input.last[6, 2]
    logger.debug "construction new Time object with '#{year}-#{month}-#{day}'"
    Time.new(year, month, day)
  end

  #
  # Caculate ranking for this period for all age groups and save to datastore.
  #
  # @param [Time] period_to_import period to calculate a full set of rankings
  #
  def self.calculate_rankings(period, gender)
    yob_youngest_age_group = period.year - 11
    logger.info "under 11 age group for current calculation
                 has birth year #{yob_youngest_age_group}"
    birth_year_four_digits = yob_youngest_age_group.to_s

    # 1. general rankings for all age groups
    [11, 12, 13, 14, 15, 16, 17, 18].each do |age_group|
      logger.debug "calculating general rankings for U#{age_group}"
      yobs_to_fetch = yob_range_to_fetch(birth_year_four_digits,
                                         age_group, period, gender)
      rankings = rankings_for_age_range_in_period(yobs_to_fetch.sort, period)
      sorted_rankings = sort_rankings_for_age_group(rankings, age_group, false,
                                                    false, false)
      logger.info "calculated #{sorted_rankings.size} general ranking
                   entries for U#{age_group}"
      save_imported_rankings(sorted_rankings)
    end
    # 2. yob rankings
    [11, 12, 13, 14, 15, 16, 17, 18].each do |age_group|
      logger.debug "calculating yob rankings U#{age_group}"
      yobs_to_fetch = [(period.year - age_group).to_s[2, 4].to_i + gender]
      rankings = rankings_for_age_range_in_period(yobs_to_fetch.sort, period)
      sorted_rankings = sort_rankings_for_age_group(rankings, age_group, true,
                                                    false, false)
      logger.info "calculated #{sorted_rankings.size} yob ranking
                   entries for U#{age_group}"
      save_imported_rankings(sorted_rankings)
    end
    # 3. age group rankings
    [12, 14, 16, 18].each do |age_group|
      logger.debug "calculating age group rankings U#{age_group}"
      yobs_to_fetch = [(period.year - age_group).to_s[2, 4].to_i + gender,
                       (period.year - age_group).to_s[2, 4].to_i + gender + 1]
      rankings = rankings_for_age_range_in_period(yobs_to_fetch.sort, period)
      sorted_rankings = sort_rankings_for_age_group(rankings, age_group, false,
                                                    true, false)
      logger.info "calculated #{sorted_rankings.size} age group ranking
                   entries for U#{age_group}"
      save_imported_rankings(sorted_rankings)
    end

    # 4. in case we are importing the last quarter -
    # final listings for official age groups
    return unless period.month.eql?(1)

    # this is the final update for this year!
    ye_date = period - 1.day
    [12, 14, 16, 18].each do |age_group|
      logger.debug "calculating year final age group rankings U#{age_group}"
      yobs_to_fetch = [(ye_date.year - age_group).to_s[2, 4].to_i + gender,
                       (ye_date.year - age_group).to_s[2, 4].to_i + gender + 1]
      rankings = rankings_for_age_range_in_period(yobs_to_fetch.sort, period)
      sorted_rankings = sort_rankings_for_age_group(rankings, age_group, false,
                                                    true, true)
      logger.info "calculated #{sorted_rankings.size} year final age group
                   ranking entries for U#{age_group}"
      save_imported_rankings(sorted_rankings)
    end
  end

  #
  # Save a set of rankings for a period.
  # This method assumes all rankings are of the same ranking class.
  #
  # @param [Array] updated_rankings array of rankings
  #
  def self.save_imported_rankings(updated_rankings)
    return if updated_rankings.empty?

    updated_rankings.each do |imported|
      ranking = create_ranking(imported)
      ranking.save
    end
  end

  def self.create_ranking(imported_ranking)
    Ranking.new(dtb_id: imported_ranking.dtb_id,
                date: imported_ranking.date,
                age_group: imported_ranking.age_group,
                ranking_position: imported_ranking.ranking_position,
                score: imported_ranking.score,
                age_group_ranking: imported_ranking.age_group_ranking,
                yob_ranking: imported_ranking.yob_ranking,
                year_end_ranking: imported_ranking.year_end_ranking)
  end

  def self.rankings_for_age_range_in_period(classes_to_retrieve, period)
    rankings = []
    id_start = classes_to_retrieve.first * 100_000
    id_end = classes_to_retrieve.last * 100_000 + 99_999
    rankings_from_db = load_ranking_for_age_range(id_start, id_end, period)
    rankings_from_db.each do |re|
      rankings.push(ImportRanking.new(dtb_id: re.dtb_id, date: re.date,
                                      age_group: re.age_group, score: re.score,
                                      ranking_position: re.ranking_position))
    end
    rankings
  end

  def self.load_ranking_for_age_range(dtb_id_start, dtb_id_end, period)
    Ranking.where(date: period,
                  dtb_id: dtb_id_start...dtb_id_end,
                  age_group: 'Overall')
           .order(:ranking_position, dtb_id: :desc)
  end

  private_class_method :create_time_from_string, :calculate_rankings
  private_class_method :save_imported_rankings, :create_ranking
  private_class_method :load_ranking_for_age_range, :create_import_player
  private_class_method :rankings_for_age_range_in_period
end
