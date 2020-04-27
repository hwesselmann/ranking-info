require 'test_helper'

class FederationsControllerTest < ActionDispatch::IntegrationTest
  test 'should get index' do
    get federations_url
    assert_response :success
    assert_select 'title', full_title('Verbandsdaten')
  end
end
