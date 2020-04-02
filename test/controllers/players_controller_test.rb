require 'test_helper'

class PlayersControllerTest < ActionDispatch::IntegrationTest
  test 'should get players' do
    get players_path
    assert_response :success
    assert_select 'title', full_title('Spieler')
  end
end
