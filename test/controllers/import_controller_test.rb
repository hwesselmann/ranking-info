require 'test_helper'

class ImportControllerTest < ActionDispatch::IntegrationTest
  test 'should get status' do
    get status_path
    assert_response :success
    assert_select 'title', full_title('Status')
  end
end
