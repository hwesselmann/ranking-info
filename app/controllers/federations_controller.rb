class FederationsController < ApplicationController
  def index
    @federations = {}
    quarter = Ranking.select(:date).order(date: :desc)
                     .distinct.first
    return @federations if quarter.nil?

    quarter = quarter.date
    sql = "SELECT COUNT(dtb_id) AS count, federation, age_group FROM rankings
           WHERE date='#{quarter}'
           AND dtb_id >= 10000000
           AND dtb_id < 20000000
           AND yob_ranking=false
           AND age_group_ranking=true
           AND year_end_ranking=false
           GROUP BY federation, age_group;"
    male_count = ActiveRecord::Base.connection.exec_query(sql)

    sql = "SELECT COUNT(dtb_id) AS count, federation, age_group FROM rankings
           WHERE date='#{quarter}'
           AND dtb_id >= 20000000
           AND dtb_id < 30000000
           AND yob_ranking=false
           AND age_group_ranking=true
           AND year_end_ranking=false
           GROUP BY federation, age_group;"
    female_count = ActiveRecord::Base.connection.exec_query(sql)

    federations = {}
    age_groups = {}
    curr_fed = ''
    # male counts
    male_count.each do |entry|
      unless entry['federation'].eql?(curr_fed)
        federations[curr_fed] = age_groups unless curr_fed.eql?('')
        age_groups = {}
        curr_fed = entry['federation']
      end
      age_group = "#{entry['age_group']}m"
      age_groups[age_group] = entry['count']
    end
    federations[curr_fed] = age_groups

    # female counts
    age_groups = {}
    female_count.each do |entry|
      age_group = "#{entry['age_group']}w"
      federations[entry['federation']][age_group] = entry['count']
    end
    @federations = federations
  end
end
