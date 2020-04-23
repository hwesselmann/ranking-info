require 'test_helper'

class ClubsSearchTest < ActionDispatch::IntegrationTest
  test 'check club search page' do
    get clubs_path
    assert_template 'clubs/index'
    assert assigns(:clubs)
    assert_template partial: 'clubs/_search_intro'
  end

  test 'check club result list' do
    get clubs_path, params: { commit: 'suchen', club: 'Must' }
    assert_response :success
    assert assigns(:clubs)
    assert_template 'clubs/index'
    assert_select 'div', 'Es wurden keine Vereine gefunden.'
  end
end
