require 'test_helper'

class ApplicationHelperTest < ActionView::TestCase
  test 'full title helper' do
    assert_equal full_title, 'Ranking-Info'
    assert_equal full_title('About'), 'About | Ranking-Info'
  end
end
