class CreatePlayers < ActiveRecord::Migration[6.0]
  def change
    create_table :players do |t|
      t.integer :dtb_id, uniqueness: true, index: true
      t.string :firstname, limit: 50
      t.string :lastname, limit: 50
      t.string :federation, limit: 50
      t.string :club
      t.string :nationality, limit: 3
      t.timestamps
    end
  end
end
