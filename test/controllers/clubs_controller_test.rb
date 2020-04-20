# frozen_string_literal: true

require 'test_helper'

class ClubsControllerTest < ActionDispatch::IntegrationTest
  test 'should get clubs' do
    get clubs_path
    assert_response :success
    assert_select 'title', full_title('Vereinssuche')
  end

  test 'get club info' do
    get club_path('Eintracht Frankfurt')
    assert_response :success
    assert_select 'title', full_title('Eintracht Frankfurt')
  end
end
