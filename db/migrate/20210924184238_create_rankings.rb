class CreateRankings < ActiveRecord::Migration[6.0]
  def change
    create_table :rankings do |t|
      t.integer :dtb_id, index: true
      t.string :firstname, limit: 50
      t.string :lastname, limit: 50, index: true
      t.string :federation, limit: 50, index: true
      t.string :club, index: true
      t.string :nationality, limit: 3
      t.date :date
      t.string :age_group
      t.integer :ranking_position
      t.string :score
      t.boolean :age_group_ranking, default: false
      t.boolean :yob_ranking, default: false
      t.boolean :year_end_ranking, default: false

      t.timestamps
    end
    add_index :rankings, [:dtb_id, :date]
    add_index :rankings, [:dtb_id, :date, :federation]
  end
end
