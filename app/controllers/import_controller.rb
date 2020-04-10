class ImportController < ApplicationController
  def redirect(url, text)
    redirect_to url, notice: text
  end

  def info
    # show some stats
    @player_male_count = Player.where("dtb_id LIKE '1%'").count
    @player_female_count = Player.where("dtb_id LIKE '2%'").count
    @ranking_count = Ranking.count
    @available_quarters = helpers.fetch_available_quarters

    # last import
    last_updated = Ranking.order(updated_at: :desc).first
    if last_updated.nil?
      last_updated = ''
    else
      # convert to CEST time
      last_updated = last_updated.updated_at.localtime('+02:00')
      last_updated = last_updated.strftime('%d.%m.%Y %H:%M')
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
      redirect(status_url, 'new rankings imported!')
    else
      redirect(status_url, "please upload a ranking file")
    end
  end
end
