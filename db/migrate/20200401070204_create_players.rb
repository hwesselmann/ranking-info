class CreatePlayers < ActiveRecord::Migration[6.0]
  def change
    create_table :players do |t|
      t.integer :dtb_id, uniqueness: true, index: true
      t.string :firstname, null: false, limit: 50
      t.string :lastname, null: false, limit: 50
      t.string :federation, null: false, limit: 50
      t.string :club, null: false
      t.string :nationality, null: false, limit: 3
      t.timestamps
    end
  end
end
