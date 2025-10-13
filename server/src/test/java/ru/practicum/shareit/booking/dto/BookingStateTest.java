package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class BookingStateTest {

    @Test
    void values_ShouldReturnAllEnumValues() {
        BookingState[] values = BookingState.values();

        assertEquals(6, values.length);
        assertArrayEquals(new BookingState[]{
                BookingState.ALL,
                BookingState.CURRENT,
                BookingState.PAST,
                BookingState.FUTURE,
                BookingState.WAITING,
                BookingState.REJECTED
        }, values);
    }

    @ParameterizedTest
    @EnumSource(BookingState.class)
    void from_ShouldReturnOptionalWithState_WhenValidStateNameProvided(BookingState state) {
        String stateName = state.name();

        Optional<BookingState> result = BookingState.from(stateName);

        assertTrue(result.isPresent());
        assertEquals(state, result.get());
    }

    @ParameterizedTest
    @EnumSource(BookingState.class)
    void from_ShouldReturnOptionalWithState_WhenValidStateNameInLowerCase(BookingState state) {
        String stateName = state.name().toLowerCase();

        Optional<BookingState> result = BookingState.from(stateName);

        assertTrue(result.isPresent());
        assertEquals(state, result.get());
    }

    @ParameterizedTest
    @EnumSource(BookingState.class)
    void from_ShouldReturnOptionalWithState_WhenValidStateNameInMixedCase(BookingState state) {
        String stateName = state.name().charAt(0) + state.name().substring(1).toLowerCase();

        Optional<BookingState> result = BookingState.from(stateName);

        assertTrue(result.isPresent());
        assertEquals(state, result.get());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "INVALID", "UNKNOWN", "PENDING", "APPROVED", "all ", " ALL"})
    void from_ShouldReturnEmptyOptional_WhenInvalidStateNameProvided(String invalidState) {
        Optional<BookingState> result = BookingState.from(invalidState);

        assertFalse(result.isPresent());
    }

    @Test
    void from_ShouldReturnEmptyOptional_WhenNullProvided() {
        Optional<BookingState> result = BookingState.from(null);

        assertFalse(result.isPresent());
    }

    @Test
    void from_ShouldBeCaseInsensitive() {
        String[] testCases = {"all", "All", "ALL", "AlL"};

        for (String testCase : testCases) {
            Optional<BookingState> result = BookingState.from(testCase);

            assertTrue(result.isPresent(), "Should find state for: " + testCase);
            assertEquals(BookingState.ALL, result.get());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"waiting", "WAITING", "Waiting"})
    void from_ShouldCorrectlyParseWaitingState(String stateVariation) {
        Optional<BookingState> result = BookingState.from(stateVariation);

        assertTrue(result.isPresent());
        assertEquals(BookingState.WAITING, result.get());
    }

    @ParameterizedTest
    @ValueSource(strings = {"rejected", "REJECTED", "Rejected"})
    void from_ShouldCorrectlyParseRejectedState(String stateVariation) {
        Optional<BookingState> result = BookingState.from(stateVariation);

        assertTrue(result.isPresent());
        assertEquals(BookingState.REJECTED, result.get());
    }
}