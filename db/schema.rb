# This file is auto-generated from the current state of the database. Instead
# of editing this file, please use the migrations feature of Active Record to
# incrementally modify your database, and then regenerate this schema definition.
#
# This file is the source Rails uses to define your schema when running `bin/rails
# db:schema:load`. When creating a new database, `bin/rails db:schema:load` tends to
# be faster and is potentially less error prone than running all of your
# migrations from scratch. Old migrations may fail to apply correctly if those
# migrations use external dependencies or application code.
#
# It's strongly recommended that you check this file into your version control system.

ActiveRecord::Schema[7.0].define(version: 2022_07_20_150031) do
  create_table "rankings", force: :cascade do |t|
    t.integer "dtb_id"
    t.string "firstname", limit: 50
    t.string "lastname", limit: 50
    t.string "federation", limit: 50
    t.string "club"
    t.string "nationality", limit: 3
    t.date "date"
    t.string "age_group"
    t.integer "ranking_position"
    t.string "score"
    t.boolean "age_group_ranking", default: false
    t.boolean "yob_ranking", default: false
    t.boolean "year_end_ranking", default: false
    t.datetime "created_at", null: false
    t.datetime "updated_at", null: false
    t.index ["age_group", "age_group_ranking", "yob_ranking", "year_end_ranking", "date"], name: "age_group_all_options"
    t.index ["age_group", "date"], name: "index_rankings_on_age_group_and_date"
    t.index ["club"], name: "index_rankings_on_club"
    t.index ["dtb_id", "date", "federation"], name: "index_rankings_on_dtb_id_and_date_and_federation"
    t.index ["dtb_id", "date"], name: "index_rankings_on_dtb_id_and_date"
    t.index ["dtb_id"], name: "index_rankings_on_dtb_id"
    t.index ["federation"], name: "index_rankings_on_federation"
    t.index ["lastname"], name: "index_rankings_on_lastname"
  end

  create_table "users", force: :cascade do |t|
    t.string "name"
    t.string "email"
    t.datetime "created_at", null: false
    t.datetime "updated_at", null: false
    t.string "password_digest"
    t.index ["email"], name: "index_users_on_email", unique: true
  end

end
