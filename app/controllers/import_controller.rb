class ImportController < ApplicationController
  def redirect(url, text)
    redirect_to url, notice: text
  end

  def info
    # show some stats
    @player_count = Player.count
    @ranking_count = Ranking.count
    # available rankings
    # last import
  end

  def import
    if params[:file]
      uploaded_file = params[:file]
      File.open(Rails.root.join('public', 'uploads', uploaded_file.original_filename), 'wb') do |file|
        file.write(uploaded_file.read)
      end
      Player.import_rankings('public/uploads/' + uploaded_file.original_filename)
      redirect(root_url, 'new rankings imported!')
    else
      redirect(root_url, "please upload a ranking file")
    end
  end
end
