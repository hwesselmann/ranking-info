class ListingController < ApplicationController
  def index
    @quarters = fetch_available_quarters
    @federations = []
    players = Player.select(:federation).order(:federation).distinct
    players.each do |player|
      @federations.push player.federation
    end
    if params[:commit]
      # we have a filter request - let's evaluate the params
      query = ''
      # 1. quarter => required
      query += "date='#{params[:quarter]}'"
      # 2. gender => required
      query += if params[:gender].eql?('Junioren') then " AND dtb_id LIKE '1%'"
               else " AND dtb_id LIKE '2%'"
               end
      # 3. age group => a value will be used in the query in any case
      query += if params[:age_group].eql?('') then " AND age_group='Overall'"
               else " AND age_group='#{params[:age_group]}'"
               end
      # 4. yob-ranking?
      age_group = params[:age_group][1, 2].to_i
      # U11, U13, U15, U17:
      #               '': true, false
      #               'only_yob': true, false
      #               'include_younger': false, false
      # U12, U14, U16, U18:p
      #               '':  false, true
      #               'only_yob': true, false
      #               'include_younger': false, false
      query += if params[:age_group_options].eql?('') && age_group.even? then " AND yob_ranking=false AND age_group_ranking=true"
               elsif params[:age_group_options].eql?('') && age_group.odd? then " AND yob_ranking=true AND age_group_ranking=false"
               elsif params[:age_group_options].eql?('only_yob') then " AND yob_ranking=true AND age_group_ranking=false"
               elsif params[:age_group_options].eql?('include_younger') then " AND yob_ranking=false AND age_group_ranking=false"
               end
      # 5. federation
      query += if params[:federation].eql?('') then ""
               else " AND dtb_id IN (SELECT dtb_id FROM players WHERE federation='#{params[:federation]}')"
               end
      # 6. year end ranking
      # if year end lists should be shown and the quarter selected is a Q4...
      query += if params[:year_end].eql?('1') && params[:quarter].split('-')[1].eql?('01') then " AND year_end_ranking=true"
               else " AND year_end_ranking=false"
               end
      # run the query!
      @rankings = Ranking.where(query).order(:ranking_position, score: :desc)
    end
  end

  private

  def fetch_available_quarters
    available_rankings = Ranking.select(:date).order(date: :desc).distinct
    years = []
    quarters = {}
    available_rankings.each do |ar|
      years.push((ar.date - 1.day).year.to_s) unless ar.date.month.eql?(12)
    end
    years.each do |y|
      q = []
      available_rankings.each do |ar|
        if y.eql?((ar.date - 1.day).year.to_s)
          # do not show year end ranking dates - they are triggered via checkbox
          unless ar.date.month.eql?(12) && ar.date.day.eql?(31)
            q.push([(ar.date - 1.day).strftime('%d.%m.%Y'), (ar.date).strftime('%Y-%m-%d')])
          end
        end
      end
      quarters[y] = q
    end
    quarters
  end
end
