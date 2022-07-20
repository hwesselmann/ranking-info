class AddIndexToRankings < ActiveRecord::Migration[7.0]
  def change
    add_index :rankings, [:age_group, :date]
    add_index :rankings, [:age_group, :age_group_ranking, :yob_ranking, :year_end_ranking, :date], name: 'age_group_all_options'
  end
end
