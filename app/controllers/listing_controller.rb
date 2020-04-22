# frozen_string_literal: true

#
# Controller covering all actions concerning the ranking list.
#
class ListingController < ApplicationController
  def index
    @quarters = fetch_available_quarters
    @federations = federations

    return unless params[:commit]

    # we have a filter request - let's evaluate the params
    # 1. quarter => required
    query = "date='#{params[:quarter]}'"
    # 2. gender => required
    query += gender_selected(params[:gender])
    # 3. age group => a value will be used in the query in any case
    query += age_group_selected(params[:age_group])
    # 4. age group options?
    query += age_group_options(params[:age_group][1, 2].to_i,
                               params[:age_group_options])
    # 5. federation
    query += federation_selected(params[:federation])
    # 6. club
    query += club_selected(params[:club])
    # 7. year end ranking
    # if year end lists should be shown and the quarter selected is a Q4...
    query += year_end_rankings(params[:year_end], params[:quarter])
    # run the query!
    @rankings = Ranking.where(query).order(:ranking_position, score: :desc)
  end

  private

  def fetch_available_quarters
    available_rankings = Ranking.select(:date).order(date: :desc).distinct
    years = available_years(available_rankings)
    quarters = {}
    years.each do |y|
      quarters[y] = available_quarters_for_year(y, available_rankings)
    end
    quarters
  end

  def available_years(rankings)
    years = []
    rankings.each do |r|
      years.push((r.date - 1.day).year.to_s) unless r.date.month.eql?(12)
    end
    years
  end

  def available_quarters_for_year(year, rankings)
    quarters = []
    rankings.each do |r|
      next unless year.eql?((r.date - 1.day).year.to_s)

      # do not show year end ranking dates - they are triggered via checkbox
      unless r.date.month.eql?(12) && r.date.day.eql?(31)
        quarters.push([(r.date - 1.day).strftime('%d.%m.%Y'),
                       r.date.strftime('%Y-%m-%d')])
      end
    end
    quarters
  end

  def federations
    federations = []
    players = Player.select(:federation).order(:federation).distinct
    players.each do |player|
      federations.push player.federation
    end
    federations
  end

  def gender_selected(gender)
    query = if gender.eql?('Junioren')
            then ' AND (dtb_id >= 10000000 AND dtb_id <= 19999999)'
            else ' AND (dtb_id >= 20000000 AND dtb_id <= 29999999)'
            end
    query
  end

  def age_group_selected(age_group)
    query = if age_group.eql?('') then " AND age_group='Overall'"
            else " AND age_group='#{params[:age_group]}'"
            end
    query
  end

  def age_group_options(age_group, age_group_options)
    query = if age_group_options.eql?('') && age_group.even?
            then ' AND yob_ranking=false AND age_group_ranking=true'
            elsif age_group_options.eql?('') && age_group.odd?
            then ' AND yob_ranking=true AND age_group_ranking=false'
            elsif age_group_options.eql?('only_yob')
            then ' AND yob_ranking=true AND age_group_ranking=false'
            elsif age_group_options.eql?('include_younger')
            then ' AND yob_ranking=false AND age_group_ranking=false'
            end
    query
  end

  def federation_selected(federation)
    query = if federation.eql?('') then ''
            else " AND dtb_id IN
                  (SELECT dtb_id FROM players WHERE
                  federation='#{federation}')"
            end
    query
  end

  def club_selected(club)
    query = if club.eql?('') then ''
            else " AND dtb_id IN
              (SELECT dtb_id FROM players WHERE
              club LIKE '%#{club}%')"
            end
  end

  def year_end_rankings(year_end, quarter)
    query = if year_end.eql?('1') && first_quarter?(quarter)
            then ' AND year_end_ranking=true"'
            else ' AND year_end_ranking=false'
            end
    query
  end

  def first_quarter?(quarter)
    quarter.split('-')[1].eql?('01')
  end
end
