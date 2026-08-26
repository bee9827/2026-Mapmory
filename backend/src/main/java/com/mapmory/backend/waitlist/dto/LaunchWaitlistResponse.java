package com.mapmory.backend.waitlist.dto;

public record LaunchWaitlistResponse(LaunchWaitlistStatus status) {

    public static LaunchWaitlistResponse subscribed() {
        return new LaunchWaitlistResponse(LaunchWaitlistStatus.SUBSCRIBED);
    }

    public static LaunchWaitlistResponse alreadySubscribed() {
        return new LaunchWaitlistResponse(LaunchWaitlistStatus.ALREADY_SUBSCRIBED);
    }
}
