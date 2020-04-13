class ImportController < ApplicationController
  before_action :logged_in_user, only: %i[upload import]

  def info
    # show some stats
    @player_male_count = Player.where("dtb_id LIKE '1%'").count
    @player_female_count = Player.where("dtb_id LIKE '2%'").count
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

  def upload
    # action is just for page navigation
  end

  def import
    if params[:file]
      uploaded_file = params[:file]
      File.open(Rails.root.join('public', 'uploads', uploaded_file.original_filename), 'wb') do |file|
        file.write(uploaded_file.read)
      end
      Player.import_rankings('public/uploads/' + uploaded_file.original_filename)
      redirect_to status_url, flash: { info: 'new rankings imported!' }
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
