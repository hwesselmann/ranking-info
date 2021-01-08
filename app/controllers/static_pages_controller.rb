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
    @start = if first_import.nil? then 'xx.xx.xxxx'
             else first_import.date.strftime('%d.%m.%Y')
             end
  end

  def help; end

  def about; end
end
