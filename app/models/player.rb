class Player < ApplicationRecord
  has_many :rankings, primary_key: 'dtb_id', foreign_key: 'dtb_id'
  has_many :clubs, primary_key: 'dtb_id', foreign_key: 'dtb_id'
end
