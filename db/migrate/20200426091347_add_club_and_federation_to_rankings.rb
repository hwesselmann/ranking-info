class AddClubAndFederationToRankings < ActiveRecord::Migration[6.0]
  def change
    add_column :rankings, :club, :string
    add_column :rankings, :federation, :string
  end
end
