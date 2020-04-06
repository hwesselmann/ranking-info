require 'test_helper'

class ListingControllerTest < ActionDispatch::IntegrationTest
  test 'should get listings' do
    get listings_path
    assert_response :success
    assert_select 'title', full_title('Ranglisten')
  end
end
