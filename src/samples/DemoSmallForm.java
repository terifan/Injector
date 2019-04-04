package samples;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import org.terifan.injector.Inject;
import org.terifan.injector.Injector;
import org.terifan.injector.Named;
import org.terifan.injector.PostConstruct;
import org.terifan.injector.Provider;


public class DemoSmallForm
{
	public static void main(String... args)
	{
		try
		{
			Injector injector = new Injector();

			injector.setLog(System.out);

/*
Injecting [MockUserService] instance into [UserPanel] instance field [mUserService]
Injecting [Color] instance named [foreground] into [Style] instance field [mText]
Injecting [Color] instance named [background] into [Style] instance field [mBackground]
Injecting [Style] instance into [UserPanel] instance field [mStyle]
Invoking PostConstruct method [buildForm] in instance of [UserPanel]

UserPanel.mUserService = new MockUserService {}
UserPanel.mStyle = new Style {
	mText => (Color)"foreground"
	mBackground -> (Color)"background"
}
UserPanel.buildForm()
*/

			// normal running
//			injector.bind(UserService.class).asSingleton();

			// when developing & testing
			injector.bind(UserService.class).toInstance(new MockUserService(new User("dave", "asasasasas asasasasas"), new User("steve", "ghghghghgh ghghghgh ghghghgh")));

			injector.bind(Color.class).named("background").toInstance(Color.RED);
			injector.bind(Color.class).named("foreground").toProvider(()->Color.BLUE);

			injector.bindConstant().named("textSize").to(27f);

			// replace style
//			injector.bind(Style.class).toInstance(new Style(Color.RED, Color.BLUE));

			UserPanel panel = injector.getInstance(UserPanel.class);

			JFrame frame = new JFrame();
			frame.add(panel);
			frame.setSize(1024, 768);
			frame.setLocationRelativeTo(null);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setVisible(true);
		}
		catch (Throwable e)
		{
			e.printStackTrace(System.out);
		}
	}


	static class Style
	{
		@Inject @Named("foreground") Color mText;
		@Inject @Named("background") Color mBackground;
		@Inject @Named("textSize") float mTextSize = 12f;

		public Style()
		{
		}

		public Style(Color aText, Color aBackground)
		{
			mText = aText;
			mBackground = aBackground;
		}
	}


	static class UserPanel extends JPanel
	{
		@Inject private Provider<UserService> mUserService;
		@Inject private Style mStyle;
		private User mUser;


		@PostConstruct
		public void buildForm()
		{
			JList<User> list = new JList<>(mUserService.get().getUsers());

			JTextArea text = new JTextArea();
			text.setForeground(mStyle.mText);
			text.setBackground(mStyle.mBackground);
			text.setFont(text.getFont().deriveFont(mStyle.mTextSize));

			list.addListSelectionListener(aEvent ->
			{
				if (mUser != null && !mUser.mDescription.equals(text.getText()))
				{
					mUser.mDescription = text.getText();
					mUserService.get().save(mUser);
				}

				mUser = list.getModel().getElementAt(list.getSelectedIndex());

				text.setText(mUser.mDescription);
			});

			super.setLayout(new BorderLayout());
			super.add(new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(list), new JScrollPane(text)));
		}
	}


	static class MockUserService extends UserService
	{
		User[] mUsers;


		public MockUserService(User... aUsers)
		{
			mUsers = aUsers;
		}


		@Override
		public User[] getUsers()
		{
			return mUsers;
		}


		@Override
		public void save(User aUser)
		{
			System.out.println("saved user: " + aUser + "=" + aUser.mDescription);
		}
	}


	static class UserService
	{
		User[] getUsers()
		{
			throw new UnsupportedOperationException();
		}


		void save(User aUser)
		{
			throw new UnsupportedOperationException();
		}
	}


	static class User
	{
		String mName;
		String mDescription;


		public User(String aName, String aDescription)
		{
			mName = aName;
			mDescription = aDescription;
		}


		@Override
		public String toString()
		{
			return mName;
		}
	}
}
