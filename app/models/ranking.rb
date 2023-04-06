# frozen_string_literal: true

require 'csv'

#
# a model for a ranking - the main item of this software
# this model includes some class methods to import new rankings
#
class Ranking < ApplicationRecord
  def self.import_rankings(file)
    period_to_import = extract_period_from_filename(file)
    store_rankings_from_csv(file, period_to_import)
    gender_factor = extract_gender_from_filename(file)
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
  # Save ranking entries for a period into the dabatase.
  #
  # @param [String] name of the file to import from
  # @param [Time] period_to_import period for the current data import
  #
  def self.store_rankings_from_csv(file, period_to_import)
    input = CSV.read(file, encoding: 'utf-8')
    input.each do |entry|
      save_ranking(entry, period_to_import)
    end
  end

  def self.save_ranking(entry, period_to_import)
    nat = if entry[3].nil?
            'nil'
          else
            entry[3]
          end
    Ranking.create(dtb_id: entry[4].split(' ').first.to_i,
                   age_group: 'overall',
                   date: period_to_import,
                   ranking_position: entry[0].to_i,
                   lastname: entry[1],
                   firstname: entry[2],
                   nationality: nat,
                   score: entry[6],
                   age_group_ranking: false,
                   yob_ranking: false,
                   year_end_ranking: false,
                   club: entry[5],
                   federation: entry[4].split(' ').last)
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
      rankings_for_age_range_in_period(yobs_to_fetch.sort, period, age_group,
                                       false, false, false)
    end
    # 2. yob rankings
    [11, 12, 13, 14, 15, 16, 17, 18].each do |age_group|
      logger.debug "calculating yob rankings U#{age_group}"
      yobs_to_fetch = [(period.year - age_group).to_s[2, 4].to_i + gender]
      rankings_for_age_range_in_period(yobs_to_fetch.sort, period, age_group,
                                       true, false, false)
    end
    # 3. age group rankings
    [12, 14, 16, 18].each do |age_group|
      logger.debug "calculating age group rankings U#{age_group}"
      yobs_to_fetch = [(period.year - age_group).to_s[2, 4].to_i + gender,
                       (period.year - age_group).to_s[2, 4].to_i + gender + 1]
      rankings_for_age_range_in_period(yobs_to_fetch.sort, period, age_group,
                                       false, true, false)
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
      rankings_for_age_range_in_period(yobs_to_fetch.sort, period, age_group,
                                       false, true, true)
    end
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

  def self.rankings_for_age_range_in_period(classes_to_retrieve, period, age_group, is_yob_ranking, is_age_group_ranking, year_end_ranking)
    id_start = classes_to_retrieve.first * 100_000
    id_end = classes_to_retrieve.last * 100_000 + 99_999
    rankings_from_db = load_ranking_for_age_range(id_start, id_end, period)
    last_rank = 0
    start_ranking = 0
    count_up = 1
    rankings_from_db.each do |curr_ranking|
      ranking = Ranking.new
      if curr_ranking.ranking_position > last_rank
        last_rank = curr_ranking.ranking_position
        start_ranking = count_up
      end
      ranking.age_group = "U#{age_group}"
      ranking.date = curr_ranking.date
      ranking.yob_ranking = is_yob_ranking
      ranking.age_group_ranking = is_age_group_ranking
      ranking.year_end_ranking = year_end_ranking
      ranking.ranking_position = start_ranking
      ranking.dtb_id = curr_ranking.dtb_id
      ranking.lastname = curr_ranking.lastname
      ranking.firstname = curr_ranking.firstname
      ranking.nationality = curr_ranking.nationality
      ranking.club = curr_ranking.club
      ranking.federation = curr_ranking.federation
      ranking.score = curr_ranking.score
      # ranking is only counted up for German players,
      # foreign player get the right position, but are not adding
      # up to the actual position, also players with pr or Einstufung do not count
      if curr_ranking.nationality.eql?('GER') && !curr_ranking.score.eql?('0,0') &&
         !curr_ranking.score.eql?('PR') && !curr_ranking.score.eql?('Einst.')
        count_up += 1
      end
      ranking.save
    end
  end

  def self.load_ranking_for_age_range(dtb_id_start, dtb_id_end, period)
    Ranking.where(date: period,
                  dtb_id: dtb_id_start...dtb_id_end,
                  age_group: 'overall')
           .order(:ranking_position, dtb_id: :desc)
  end

  #
  # Extract the gender from the filename.
  #
  # @param [String] filename filename to extract period from
  #
  # @return [Integer] gender factor
  #
  def self.extract_gender_from_filename(filename)
    logger.debug "extracting gender part from file '#{filename}'"
    filename_without_path = filename.split('/').last
    if filename_without_path.include? 'Junioren'
      100
    else
      200
    end
  end

  private_class_method :create_time_from_string, :calculate_rankings
  private_class_method :load_ranking_for_age_range, :save_ranking
  private_class_method :rankings_for_age_range_in_period
  private_class_method :extract_gender_from_filename
end
