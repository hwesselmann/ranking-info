class ImportController < ApplicationController
  def redirect(url, text)
    redirect_to url, notice: text
  end

  def info
    # show some stats
    @player_male_count = Player.where("dtb_id LIKE '1%'").count
    @player_female_count = Player.where("dtb_id LIKE '2%'").count
    @ranking_count = Ranking.count
    # available rankings
    available_rankings = Ranking.select(:date).order(date: :desc).distinct
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
    @available_quarters = years

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
