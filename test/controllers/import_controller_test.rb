require 'test_helper'

class ImportControllerTest < ActionDispatch::IntegrationTest
  def setup
    @user = users(:testuser)
  end

  test 'should get status' do
    get status_path
    assert_response :success
    assert_select 'title', full_title('Status')
  end

  test 'should redirect update when not logged in' do
    get import_path
    assert_not flash.empty?
    assert_redirected_to login_url
  end
end
