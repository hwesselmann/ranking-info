# frozen_string_literal: true

#
# Rails application helper.
#
module ApplicationHelper
  # Returns the full title on a per-page basis.
  def full_title(page_title = '')
    base_title = 'Ranking-Info'
    if page_title.empty?
      base_title
    else
      page_title + ' | ' + base_title
    end
  end

  # fetch available quarters generally or for a player if dtb_id is given
  def fetch_available_quarters(dtb_id: '')
    available_rankings = load_rankings(dtb_id)
    year = 0
    years = {}
    quarters = []
    available_rankings.each do |ar|
      unless ar.date.year.eql?(year)
        # new year, shift
        unless year.eql?(0)
          years[year.to_s] = quarters.reverse
          quarters.clear
        end
        year = ar.date.year
      end
      quarters.push ar.date.strftime('%d.%m.')
    end
    years[year.to_s] = quarters.reverse
    years
  end

  def load_rankings(dtb_id)
    rankings = if dtb_id.eql?('')
               then Ranking.select(:date).order(date: :desc).distinct
               else Ranking.select(:date)
                           .where(dtb_id: dtb_id)
                           .order(date: :desc)
                           .distinct
               end
    rankings
  end
end
