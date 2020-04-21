# frozen_string_literal: true

#
# Controller for static content.
#
class StaticPagesController < ApplicationController
  def home
    first_import = Ranking.select(:date)
                          .order(date: :desc)
                          .distinct
                          .last
                          .date
    @start = if first_import.nil? then 'xx.xx.xxxx'
             else first_import.strftime('%d.%m.%Y')
             end
  end

  def about; end
end
