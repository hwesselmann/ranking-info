class CreateClubs < ActiveRecord::Migration[6.0]
  def change
    create_table :clubs do |t|
      t.integer :dtb_id, index: true
      t.string :club
      t.string :federation
    end
  end
end
