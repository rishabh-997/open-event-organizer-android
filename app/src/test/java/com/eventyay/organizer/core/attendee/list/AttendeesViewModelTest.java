package com.eventyay.organizer.core.attendee.list;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.eventyay.organizer.data.Preferences;
import com.eventyay.organizer.data.attendee.Attendee;
import com.eventyay.organizer.data.attendee.AttendeeRepository;
import com.eventyay.organizer.data.db.DatabaseChangeListener;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.plugins.RxAndroidPlugins;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;

@RunWith(JUnit4.class)
public class AttendeesViewModelTest
{
    @Rule public MockitoRule mockitoRule = MockitoJUnit.rule();
    @Rule public TestRule rule = new InstantTaskExecutorRule();

    @Mock private AttendeeRepository attendeeRepository;
    @Mock private DatabaseChangeListener<Attendee> attendeeListener;
    @Mock private Preferences preferences;

    @Mock Observer<String> error;
    @Mock Observer<Boolean> progress;
    @Mock Observer<Attendee> attendee;
    @Mock Observer<Boolean> showScanButton;
    @Mock Observer<List<Attendee>> attendeeList;

    private static final List<Attendee> ATTENDEES = Arrays.asList(
        Attendee.builder().isCheckedIn(false).build(),
        Attendee.builder().isCheckedIn(true).build(),
        Attendee.builder().isCheckedIn(false).build(),
        Attendee.builder().isCheckedIn(false).build(),
        Attendee.builder().isCheckedIn(true).build(),
        Attendee.builder().isCheckedIn(true).build(),
        Attendee.builder().isCheckedIn(false).build()
    );
    private static final long EVENT_ID = 5L;

    private AttendeesViewModel attendeesViewModel;

    @Before
    public void setUp() {
        RxJavaPlugins.setIoSchedulerHandler(scheduler -> Schedulers.trampoline());
        RxJavaPlugins.setComputationSchedulerHandler(scheduler -> Schedulers.trampoline());
        RxAndroidPlugins.setInitMainThreadSchedulerHandler(schedulerCallable -> Schedulers.trampoline());

        attendeesViewModel = new AttendeesViewModel(attendeeRepository,attendeeListener,preferences);
    }

    @After
    public void tearDown() {
        RxJavaPlugins.reset();
        RxAndroidPlugins.reset();
    }

    @Test
    public void shouldLoadAttendeeSuccessfully () {
        when(attendeeRepository.getAttendee(EVENT_ID,false))
            .thenReturn(Observable.fromIterable(ATTENDEES));

        InOrder inOrder = Mockito.inOrder(attendeeList, attendeeRepository, progress,showScanButton);

        attendeesViewModel.getProgress().observeForever(progress);
        attendeesViewModel.getShowScanButtonLiveData().observeForever(showScanButton);
        attendeesViewModel.getAttendeesLiveData().observeForever(attendeeList);

        attendeesViewModel.loadAttendees(false);

        inOrder.verify(attendeeList).onChanged(ATTENDEES);
        inOrder.verify(attendeeRepository).getAttendee(EVENT_ID,false);
        inOrder.verify(progress).onChanged(true);
        inOrder.verify(progress).onChanged(false);
        inOrder.verify(showScanButton).onChanged(true);
    }

    @Test
    public void shouldLoadAttendeePageWiseSuccesfully() {
        when(attendeeRepository.getAttendeesPageWise(EVENT_ID,2,false))
            .thenReturn(Observable.fromIterable(ATTENDEES));

        InOrder inOrder = Mockito.inOrder(attendeeList, attendeeRepository, progress,showScanButton);

        attendeesViewModel.getProgress().observeForever(progress);
        attendeesViewModel.getShowScanButtonLiveData().observeForever(showScanButton);
        attendeesViewModel.getAttendeesLiveData().observeForever(attendeeList);

        attendeesViewModel.loadAttendees(false);

        inOrder.verify(attendeeList).onChanged(ATTENDEES);
        inOrder.verify(attendeeRepository).getAttendee(EVENT_ID,false);
        inOrder.verify(progress).onChanged(true);
        inOrder.verify(progress).onChanged(false);
        inOrder.verify(showScanButton).onChanged(true);
    }

    @Test
    public void shouldShowErrorOnFailure() {
        when(attendeeRepository.getAttendee(EVENT_ID, false))
            .thenReturn(Observable.error(new Throwable("Error")));

        InOrder inOrder = Mockito.inOrder(attendeeRepository, progress,error);

        attendeesViewModel.getProgress().observeForever(progress);
        attendeesViewModel.getError().observeForever(error);
        attendeesViewModel.getShowScanButtonLiveData().observeForever(showScanButton);

        inOrder.verify(attendeeRepository).getAttendee(EVENT_ID,false);
        inOrder.verify(progress).onChanged(true);
        inOrder.verify(error).onChanged("Error");
        inOrder.verify(progress).onChanged(false);
    }
}
