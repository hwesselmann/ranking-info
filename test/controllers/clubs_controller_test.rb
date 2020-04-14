require 'test_helper'

class ClubsControllerTest < ActionDispatch::IntegrationTest
  test 'should get clubs' do
    get clubs_path
    assert_response :success
    assert_select 'title', full_title('Vereinssuche')
  end
end
