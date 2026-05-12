
###  Chat Feed Strings  ###
|**Keyword**|**Default Value**|**Description**|
|-|-|-|
|smi_chat_feed_title|Chat|Title for the chat feed view.|
|smi_chat_feed_accessibility|Chat Feed|Accessibility label for the chat feed container view.|
|smi_chat_add_button|Open option menu.|Content description for the add button in the chat view to open the options menu.|
|smi_chat_start_conversation|Start Conversation|Content description for a button that is used to resubmit pre-chat fields mid-conversation.|
|smi_chat_start_conversation_feed_input_text_accessibility|Fill out a pre-chat form to continue the conversation|Accessibility announcement when the user needs to enter and resubmit pre-chat fields.|
|smi_chat_closed_conversation|This conversation has ended|Text displayed instead of the text input when a conversation has closed and can longer be interacted with.|
|smi_chat_badge_overlay_count|9+|The chat feed badge overlay unread message count for amounts equal to or over 10.|

###  Chat Feed Input Strings  ###
|**Keyword**|**Default Value**|**Description**|
|-|-|-|
|smi_feed_input_text|Type a message|Placeholder text inside chat input field.|
|smi_feed_input_text_accessibility|Message Input|Accessibility label for identifying the input control.|
|smi_feed_input_button|Send|Action text for the send message button.|
|smi_feed_new_messages|New Messages|A notification that alerts the user that there are new unread messages in the chat feed.|
|smi_feed_new_messages_accessibility|You have new messages.|An accessibility announcement that alerts the user that there are new unread messages in the chat feed.|
|smi_feed_new_messages_button_accessibility|Scroll to bottom of chat feed.|The description for the notification badge button that alerts a user they will be brought to the bottom of the feed.|

###  Action Menu Strings  ###
|**Keyword**|**Default Value**|**Description**|
|-|-|-|
|smi_feed_action_menu_photo_send|Send a Photo|Action Menu text for sending a photo.|
|smi_feed_action_menu_file_send|Send a File|Action Menu text for sending a file.|
|smi_feed_action_menu_photo_take|Take a Photo|Action Menu text for taking a photo.|
|smi_feed_action_menu_accessibility|Action Menu|The accessibility string for the Action Menu to the left of the text input|

###  Delivery Status  ###
|**Keyword**|**Default Value**|**Description**|
|-|-|-|
|smi_delivery_status_sending|Sending|The message auxiliary view that will display the sending message indicator.|
|smi_delivery_status_sent|Sent|The message auxiliary view that will display the sent message indicator.|
|smi_delivery_status_read|Read|The message auxiliary view that will display the read message indicator.|
|smi_delivery_status_delivered|Delivered|The message auxiliary view that will display the delivered message indicator.|
|smi_delivery_status_error|Send failed. Tap to retry.|The message auxiliary view that will display the error message.|
|smi_delivery_status_error_unsupportedMediaType|Message Failed.|The error message appears when a message failed due to an unsupported media type|
|smi_delivery_status_error_additional_information_required|Provide additional info to proceed.|The error message that appears when receiving a 417 error. This message corresponds to needing to resubmit pre-chat fields.|
|smi_delivery_status_error_accessibility|Failed|The concise failed status for accessibility|
|smi_delivery_status_error_hint_accessibility|Double tap to retry|Hint for the user to double tap the message to retry|
|smi_local_message_identifier_accessibility|Your Message %s|The accessibility text for identifying messages sent from the local user|
|smi_message_received_event_accessibility|Message received from %s|The accessibility announcement text that informs the user when a message has been received|
|smi_message_sending_event_accessibility|Message sent|The accessibility announcement text that informs the user that their message has been sent|

###  Network Connectivity Banner  ###
|**Keyword**|**Default Value**|**Description**|
|-|-|-|
|smi_network_status_reconnecting|Waiting to reconnect|The network connectivity banner text that will inform the user the network connection has been lost and is attempting to reconnect|
|smi_network_status_reconnecting_accessibility|Network connectivity is lost, attempting to reconnect|The accessibility announcement that informs the user when the network connection has been lost and is attempting to reconnect|
|smi_network_status_connected|Connected|The network connectivity banner that will inform the user the network has connected after the network has lost connectivity|
|smi_network_status_connected_accessibility|Network connectivity established|The accessibility announcement that informs the user when the network is connected after having lost connectivity|

###  Business Hours Banner  ###
|**Keyword**|**Default Value**|**Description**|
|-|-|-|
|smi_business_hours_outside|New chats aren\'t available outside of business hours|The message that is shown in the chat feed banner when outside of business hours|
|smi_business_hours_outside_accessibility|New chats aren\'t currently available. Try again during business hours.|The accessibility announcement that informs the user that they are out of the set business hours and the contact center is closed|

###  Typing  ###
|**Keyword**|**Default Value**|**Description**|
|-|-|-|
|smi_typing_indicator_start_accessibility|%s has started typing|The accessibility announcement that informs the user when a participant starts typing|
|smi_typing_indicator_stop_accessibility|%s has stopped typing|The accessibility announcement that informs the user when a participant stops typing|

###  Progress Indicators  ###
|**Keyword**|**Default Value**|**Description**|
|-|-|-|
|smi_progress_indicator_single_participant|%s is typing...|The default label for a single participant progress indicator.|
|smi_progress_indicator_bot|%s is responding...|The default label for a bot progress indicator.|
|smi_progress_indicator_two_participants|%1$s and %2$s are typing...|The default label for two participants typing.|
|smi_progress_indicator_multiple_participants|Multiple people are typing...|The default label for multiple participants typing.|
|smi_progress_indicator_message_accessibility|%1$s %2$s|The accessibility announcement for a bot progress indicator containing the bot display name and the progress message text.|
|smi_progress_indicator_queue_position|Your current queue position is %1$d|The current queue position label (e.g. Your current queue position is 10) displayed as a progress indicator.|

###  Misc  ###
|**Keyword**|**Default Value**|**Description**|
|-|-|-|
|smi_misc_separator|•|separator used when conjoining information together in a string|
|smi_misc_unknown|Unknown|This is used when a value or result cannot be determined|
|smi_action_cancel|Cancel|Cancel action to be used for cancel button and menus throughout the SDK.|
|smi_action_settings|Settings|Settings action for permission alerts|
|smi_file_size_megabyte|MB|Size of a file in megabytes|
|smi_unknown_entry_message|This message type isn\'t supported.|The error message that appears when the message type of the message in the chat feed isn't supported.|

###  Alerts  ###
|**Keyword**|**Default Value**|**Description**|
|-|-|-|
|smi_alert_title_failed|Message Failed|Alert title text for the retry message alert.|
|smi_alert_title_permission_camera|Allow Camera Access|Alert title text for requesting camera access.|
|smi_alert_message_failed|Message failed to send. Retry?|Confirmation text for the retry message alert.|
|smi_alert_message_permission_camera|To use your camera, go to Settings and enable Camera access for this app.|Explanation that the user must allow camera permission|
|smi_alert_action_retry|Retry|Retry action for the retry message alert.|
|smi_alert_attachment_limit_reached|Attachment limit reached|Alert when limit for number of attachments is reached.|

###  Participant Changed  ###
|**Keyword**|**Default Value**|**Description**|
|-|-|-|
|smi_participant_name_default|Participant|Default string for a participant with no displayName or subject configured.|
|smi_participant_changed_joined|%s has joined|Notice that a participant has joined the conversation|
|smi_participant_changed_joined_accessibility|%s has joined the conversation|The accessibility announcement that informs the user that a participant has joined the conversation|
|smi_participant_changed_left|%s has left|Notice that a participant has left the conversation|
|smi_participant_changed_left_accessibility|%s has left the conversation|The accessibility announcement that informs the user that a participant has left the conversation|

###  PreChat  ###
|**Keyword**|**Default Value**|**Description**|
|-|-|-|
|smi_pre_chat_required_accessibility|%s is required|The accessibility string for the PreChat field if the field is required|
|smi_pre_chat_required|%s *|The string for a PreChat field label if the field is required|
|smi_pre_chat_submit_button|Chat with an Agent|The title of the PreChat submit button|
|smi_pre_chat_title|Just a few things before we connect you with an expert…|The title of the PreChat forum|
|smi_pre_chat_choice_list_none|None|Label for the None option in a pre-chat choice list|
|smi_pre_chat_back_button|Back|The back button for the prechat form|
|smi_pre_chat_error_field_required|This field is required.|Error message shown when a required field fails PreChat validation|
|smi_pre_chat_error_valid_email_format|Use a valid email format.|Error message shown when an email PreChat field fails PreChat validation|
|smi_pre_chat_error_valid_number_format|Field can only contain numbers.|Error message shown when a number PreChat field fails PreChat validation|
|smi_pre_chat_error_valid_phone_format|Use a valid phone number format.|Error message shown when a phone PreChat field fails PreChat validation|
|smi_pre_chat_submission_review_title|Submitted Form Details|Header for prechat submitted form details|
|smi_pre_chat_submission_card_title|Form submitted Tap to review|Text for prechat cell that shows in chat after submitting prechat|
|smi_back_button_accessibility|Back to chat feed|Accessibility text for hitting the back button on pre chat submission screen|
|smi_exit_button_accessibility|Exit to chat feed|Accessibility text for hitting the exit button on pre chat submission screen|
|smi_checkbox_selected_accessibility|Checkbox selected|Accessibility text for when the checkbox is selected|
|smi_checkbox_not_selected_accessibility|Checkbox not selected|Accessibility text for when the checkbox is not selected|
|smi_terms_and_conditions_error|Accept the Terms and Conditions to continue.|Error message displayed when the Terms and Conditions are required but not accepted.|
|smi_terms_and_conditions_accept|Accept|Message displayed next to the checkbox to accept the Terms and Conditions.|
|smi_terms_and_conditions_accepted_accessibility|Terms and conditions is accepted|Accessibility label for terms and conditions box when it is accepted.|
|smi_terms_and_conditions_not_accepted_accessibility|Terms and conditions is not accepted|Accessibility label for terms and conditions box when it is accepted.|
|smi_pre_chat_error_accessibility|%1$s input. Error %2$s.|Accessibility readout for prechat errors. The first parameter is the field name, the second field is the error on the field.|

###  Attachments  ###
|**Keyword**|**Default Value**|**Description**|
|-|-|-|
|smi_image_accessibility|Image message.|Accessibility text for an image message in the chat feed or attachment preview.|
|smi_remove_image_button_accessibility|Remove image message.|Accessibility text for the button to remove an image message from the input field.|
|smi_multiple_attachment_counter|+%d|A counter that takes an integer representing the number of file attachments not shown.|
|smi_image_preview_accessibility|Image preview.|Accessibility text for the image preview dialog.|
|smi_image_preview_cancel_button_accessibility|Close image preview.|Accessibility text for the button to close the image preview dialog.|
|smi_camera_gallery_button_accessibility|Photo gallery.|Accessibility text for the button to open the photo gallery.|
|smi_camera_switch_lens_button_accessibility|Switch lens direction.|Accessibility text for the button to switch the camera lens direction.|
|smi_camera_capture_button_accessibility|Capture photo.|Accessibility text for the button to capture a photo.|
|smi_camera_view_capturing_accessibility|Camera capturing photo|Accessibility text for the view that is displaying the phones camera capturing a photo|
|smi_image_viewer_jump_to_photo_accessibility|Jump to photo|Accessibility text for the jump to photo button in image viewer|
|smi_image_viewer_share_photo_accessibility|Share photo|Accessibility text for the share button in image viewer|
|smi_image_viewer_download_photo_accessibility|Download photo|Accessibility text for the download button in image viewer|
|smi_image_saved_message|Attachment saved|Shown in popup when user taps the download button and the attachment successfully saves|
|smi_attachment_download_failed_message|Failed to save attachment|Shown in popup when user clicks on download button and the attachment fails to save|
|smi_image_download_denied_title|Allow Photo Access|Title shown in popup when user tries to save a photo with denied permission|
|smi_image_download_denied_body|To save photos, go to Settings and enable Photos access for this app.|Message shown in popup when user tries to save a photo with denied permission|
|smi_file_format_unsupported|Unsupported file format.|Message shown if user sends an unsupported file in chat|
|smi_file_format_unsupported_alternate|File type not supported, try: %s|Message show if the user sends an unsupported file in the chat and a list of alternate file types given as a suggestion.|
|smi_file_size_exceeded|Attachment is too large. Maximum size is %s.|Message shown if the user sends an attachment that exceeds the max size that can be sent|

###  Agent Transfer  ###
|**Keyword**|**Default Value**|**Description**|
|-|-|-|
|smi_transfer_requested_event_text|Transfer requested at %s|A system message informing the user that a transfer to a service agent has been requested, and a string template indicating the time of the request.|

###  Routing Failure  ###
|**Keyword**|**Default Value**|**Description**|
|-|-|-|
|smi_routing_failure_event_text|Agents are not available. Try again later.|A default system message informing the user that routing to an service console agent has failed.|

###  Routing Estimated Wait Time  ###
|**Keyword**|**Default Value**|**Description**|
|-|-|-|
|smi_routing_initial_agent|An agent will be with you shortly.|Shown when the routing type is initial. and EWT is available|
|smi_routing_wait_resolved_minutes|Your expected wait time is %s minutes.|Shown when the estimated wait time is greater than 60 seconds. Rounded up to the nearest minute|
|smi_routing_wait_resolved_short|Your expected wait time is less than one minute.|Shown when the estimated wait time is less than or equal to 60 seconds|

###  Loading Screen  ###
|**Keyword**|**Default Value**|**Description**|
|-|-|-|
|smi_loading_screen_accessibility|Active loading screen|A message that will be read out to the user when a loading screen is shown, and read out to the user when the loading screen is selected.|

###  Link Preview Strings  ###
|**Keyword**|**Default Value**|**Description**|
|-|-|-|
|smi_link_preview_accessibility|Link preview|The screen reader will read out link preview and then the name of the link|

###  Message Search  ###
|**Keyword**|**Default Value**|**Description**|
|-|-|-|
|smi_nothing_entered_in_search|Nothing here yet|Message displayed when there’s nothing in the search bar and there are no recent searches|
|smi_no_search_results|No search results|Message displayed when there are no search results|
|smi_no_matching_results|We couldn\'t find what you\'re looking for. Try searching for something else.|Message displayed when nothing matches the users search|
|smi_message_search_header|Message Search|Header for the message search page|
|smi_recent_searches_header|Recent Searches|Header for the recent search list|
|smi_search_placeholder_text|Search|Placeholder text for the search bar|
|smi_today_timestamp_message_search|Today|Timestamp text for when the message was sent today|
|smi_yesterday_timestamp_message_search|Yesterday|Timestamp text for when the message was sent yesterday|
|smi_users_name|You|The name shown for the user in the time stamp row|
|smi_no_recent_searches|You haven\'t performed a recent search. Enter keywords in the search field.|Message displayed when there are no search results and no text is entered in the search field|
|smi_message_search_back_button|Back|The back button for message search|
|smi_message_search_clear_search_text_accessibility|Clear search text|Accessibility text for clearing the search field in message search|
|smi_message_search_agent_avatar_accessibility|Agent avatar|Accessibility text for the agent avatar in message search|
|smi_message_search_user_avatar_accessibility|User avatar|Accessibility text for the user avatar in message search|
|smi_message_search_agent_bot_accessibility|Bot avatar|Accessibility text for the bot avatar in message search|

###  Copy Menu  ###
|**Keyword**|**Default Value**|**Description**|
|-|-|-|
|smi_action_copy|Copy|Text displayed in the context menu for the copy behavior|

###  Participant Client Menu  ###
|**Keyword**|**Default Value**|**Description**|
|-|-|-|
|smi_participant_client_menu_title|Conversation Options|Text displayed in the header of the participant client menu. The participant client menu will contain actions a user can take in a chat feed. The header will display the name of the participant followed by Options.|
|smi_participant_client_menu_title_accessibility|Conversation Options Menus|The accessibility message for the participant client menu|

###  Form Message  ###
|**Keyword**|**Default Value**|**Description**|
|-|-|-|
|smi_form_message_footer_submit|Submit|Text displayed in the footer menu of the Form Message for the submit button.|
|smi_form_message_footer_back|Back|Text displayed in the footer menu of the Form Message for the back button.|
|smi_form_message_footer_next|Next|Text displayed in the footer menu of the Form Message for the next button.|
|smi_form_message_back_button|Back|The back button for the secure form|
|smi_form_message_back_button_accessibility|Back to chat feed|Accessibility text for hitting the back button on form message submission screen|
|smi_form_message_exit_screen_title|Leave the form?|The main title for the secure form exit confirmation screen.|
|smi_form_message_exit_screen_subtitle|After leaving the form, you can open it again in the messaging window. However, your progress isn\'t saved.|The subtitle for the secure form exit confirmation screen.|
|smi_form_message_exit_screen_confirm|Leave Form|The leave form button for the secure form exit confirmation screen.|
|smi_form_message_exit_screen_cancel|Cancel|The cancel button text for the secure form exit confirmation screen|
|smi_form_message_result_success|Form Submitted|The message shown in the chat bubble when a form has been successfully submitted.|
|smi_form_message_result_submitting|Form Submitting|The message shown in the chat bubble when a form is submitting.|
|smi_form_message_result_error|Error Submitting Form|The message shown in the chat bubble when a form fails to submit.|
|smi_form_message_required_accessibility|%s is required|The accessibility string for the form message field if the field is required|
|smi_form_message_required|%s *|The string for a form message field label if the field is required|
|smi_form_message_section_required_acccessibility|Required: %s|The string for a form message field label if the field is required|
|smi_form_message_text_invalid_email|Not a valid email|The string that is displayed below a form entry that indicates an invalid email has been entered.|
|smi_form_message_text_invalid_url|Not a valid url|The string that is displayed below a form entry that indicates an invalid url has been entered.|
|smi_form_message_text_input_required_message|This field is required|The string that is displayed below a form entry that indicates this item is required.|
|smi_form_message_option_required_message|One or more options required|The string that is displayed below a form entry that indicates this item is required.|
|smi_form_message_divider|%1$d/%2$d|The divider for the form message text field character counter|
|smi_form_message_text_input_accessibility|%s double tap to edit|The accessibility message for the grouped form text input|
|smi_form_message_text_input_required_accessibility|%s is required. Double tap to edit|The accessibility message for the group form text required input|
|smi_form_message_screen_accessibility|%s Screen|The accessibility message is show when a new screen is displayed in the form input|
|smi_form_error_accessibility|%1$s input. Error %2$s.|Accessibility readout for form errors. The first parameter is the form field label. The second parameter is the error message on the field.|

###  Carousel  ###
|**Keyword**|**Default Value**|**Description**|
|-|-|-|
|smi_carousel_image_placeholder_accessibility|Empty Carousel image|A placeholder icon to indicate there is no image for a carousel option.|
|smi_carousel_selected_icon_accessibility|Option selected|An icon to indicate that an option has been selected for a carousel message.|
|smi_carousel_image_accessibility|Carousel image|The default image content description for a carousel option, if no description is given.|
|smi_carousel_indicator_accessibility|Page %1$d of %2$d|Accessibility label for the carousel indicator|
|smi_carousel_indicator_dot_accessibility|Go to page %1$d of %2$d|Accessibility label for individual carousel indicator dots|

###  Request Transcript  ###
|**Keyword**|**Default Value**|**Description**|
|-|-|-|
|smi_transcript_request_button|Request Chat Transcript|The title of the request transcript button in the participant client menu.|
|smi_transcript_title|Chat Transcript|The title of the request transcript screen.|
|smi_transcript_leave_button|Leave|The leave button on the leave transcript screen. This screen warns the user that if they leave without saving, the transcript is lost.|
|smi_transcript_cancel_button|Cancel|The cancel button on the leave transcript screen. This screen warns the user that if they leave without saving, the transcript is lost.|
|smi_transcript_leave_title|Leave Transcript|Title of the leave transcript screen. This screen warns the user that if they leave without saving, the transcript is lost.|
|smi_transcript_leave_subtitle|Be sure to save the transcript before leaving.|Subtitle of the leave transcript screen. This screen warns the user that if they leave without saving, the transcript is lost.|
|smi_transcript_save_button_accessibility|Save Transcript|The accessibility string for the save transcript button.|
|smi_transcript_share_button_accessibility|Share Transcript|The accessibility string for the share transcript button.|
|smi_transcript_error_message|Something went wrong. Please try again later.|Error message to display on the error screen when the transcript fails to download from the server.|
|smi_transcript_alert_save_failed|Save failed|Message to display when the request transcript fails to save on the device.|
|smi_transcript_alert_save_succeed|Save was successful|Message to display when a request transcript successfully saves on the device.|

###  End Chat Session  ###
|**Keyword**|**Default Value**|**Description**|
|-|-|-|
|smi_end_chat_menu_button|End Chat|The title of the end chat button in the participant client menu.|
|smi_end_chat_title|End Chat?|The title label on the end chat confirmation page.|
|smi_end_chat_subtitle|Are you sure you want to end the chat session? You will be disconnected from the agent.|The subtitle label on the end chat confirmation page.|
|smi_end_chat_confirmation_button|End Chat|The end chat button label on the confirmation page.|
|smi_end_chat_cancel|Cancel|The cancel button label on the confirmation page.|
|smi_end_chat_error_message|Something went wrong. Please try again later.|Error message to display on the end chat confirmation page when there is an issue ending the chat|
|smi_fallback_message_close_button|Close|The close button label on the fallback message page.|

###  Streaming Tokens  ###
|**Keyword**|**Default Value**|**Description**|
|-|-|-|
|smi_streaming_text_cursor|▏|The cursor to display while text is streaming in.|
|smi_streaming_message_accessibility|The bot is responding|Accessibility readout for when a message is being streamed in.|
|smi_streaming_invalid|The previous response couldn’t be verified. The agent is generating a new response.|Displayed when a streaming token was invalidated|
|smi_streaming_revised|Revised|Displayed when a message was revised|
|smi_citation_inline_title|\\[%d\\]|A string template for inline citations. Takes an integer representing the citation number.|
|smi_citation_source_button_title|[%1$d] %2$s|A string template for citation source button. Takes an integer representing the citation number and a string representing the url title|
|smi_session_status_changed_ended|Chat Ended • %s|Displayed in the chat feed when the chat session ends. Ex: Chat Ended • 12:34 PM|
