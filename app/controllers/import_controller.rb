# frozen_string_literal: true

#
# Controller for import and import status actions.
#
class ImportController < ApplicationController
  before_action :logged_in_user, only: %i[upload import]

  def info
    # show some stats
    @player_male_count = Ranking.select(:dtb_id).where('dtb_id >= 10000000 AND dtb_id < 20000000').distinct.count
    @player_female_count = Ranking.select(:dtb_id).where('dtb_id >= 20000000 AND dtb_id < 30000000').distinct.count
    @ranking_count = Ranking.count
    @available_quarters = helpers.fetch_available_quarters

    # last import
    last_updated_db = Ranking.order(updated_at: :desc).first
    last_updated = if last_updated_db.nil? then ''
                   else last_updated_db.updated_at.localtime('+02:00')
                                       .strftime('%d.%m.%Y %H:%M')
                   end
    @date_last_updated = last_updated
  end

  def upload; end

  def import
    if params[:file]
      uploaded = params[:file]
      File.open(Rails.root.join('public', 'uploads', uploaded.original_filename), 'wb') do |f|
        f.write(uploaded.read)
      end
      Ranking.import_rankings("public/uploads/#{uploaded.original_filename}")
      redirect_to status_url, flash: { info: "new rankings file
                                              #{uploaded.original_filename} imported!" }
    else
      redirect_to status_url, flash: { info: 'please upload a ranking file' }
    end
  end

  private

  # Confirms a logged-in user.
  def logged_in_user
    return if logged_in?

    flash[:danger] = 'Please log in to access import page'
    redirect_to login_url
  end
end
