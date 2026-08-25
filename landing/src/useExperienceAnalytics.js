import { useCallback, useEffect, useRef } from "react";
import { ANALYTICS_EVENTS, trackEvent } from "./analytics.js";

const VIEW_THRESHOLD = 0.5;
const VIEW_DURATION_MS = 1000;
const ENGAGEMENT_MILESTONES = [10, 30, 60];
const OBSERVER_THRESHOLDS = [0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.75, 1];

function occupiesEnoughOfViewport(entry) {
  const referenceHeight = Math.min(entry.boundingClientRect.height, window.innerHeight);
  return entry.isIntersecting
    && entry.intersectionRect.height >= referenceHeight * VIEW_THRESHOLD;
}

export function useExperienceAnalytics(experienceType) {
  const sectionRef = useRef(null);
  const isVisibleRef = useRef(false);
  const hasViewedRef = useRef(false);
  const hasStartedRef = useRef(false);
  const activeSecondsRef = useRef(0);
  const sentMilestonesRef = useRef(new Set());
  const viewTimerRef = useRef(null);

  useEffect(() => {
    const section = sectionRef.current;
    if (!section) return undefined;

    const clearViewTimer = () => {
      if (viewTimerRef.current) {
        window.clearTimeout(viewTimerRef.current);
        viewTimerRef.current = null;
      }
    };

    const observer = new IntersectionObserver(([entry]) => {
      isVisibleRef.current = occupiesEnoughOfViewport(entry);
      if (!isVisibleRef.current) {
        clearViewTimer();
        return;
      }

      if (!hasViewedRef.current && !viewTimerRef.current) {
        viewTimerRef.current = window.setTimeout(() => {
          if (!isVisibleRef.current || hasViewedRef.current) return;
          hasViewedRef.current = true;
          trackEvent(ANALYTICS_EVENTS.EXPERIENCE_VIEW, {
            experience_type: experienceType,
          });
        }, VIEW_DURATION_MS);
      }
    }, { threshold: OBSERVER_THRESHOLDS });

    observer.observe(section);
    return () => {
      clearViewTimer();
      observer.disconnect();
    };
  }, [experienceType]);

  useEffect(() => {
    const intervalId = window.setInterval(() => {
      if (!hasStartedRef.current || !isVisibleRef.current || document.hidden) return;

      activeSecondsRef.current += 1;
      ENGAGEMENT_MILESTONES.forEach((milestoneSeconds) => {
        if (
          activeSecondsRef.current >= milestoneSeconds
          && !sentMilestonesRef.current.has(milestoneSeconds)
        ) {
          sentMilestonesRef.current.add(milestoneSeconds);
          trackEvent(ANALYTICS_EVENTS.EXPERIENCE_ENGAGEMENT, {
            experience_type: experienceType,
            milestone_seconds: milestoneSeconds,
          });
        }
      });
    }, 1000);

    return () => window.clearInterval(intervalId);
  }, [experienceType]);

  const startExperience = useCallback((interactionType) => {
    if (hasStartedRef.current) return;
    hasStartedRef.current = true;
    trackEvent(ANALYTICS_EVENTS.EXPERIENCE_START, {
      experience_type: experienceType,
      interaction_type: interactionType,
    });
  }, [experienceType]);

  const trackPlaceSelect = useCallback((placeId, selectionSource) => {
    startExperience("place_select");
    trackEvent(ANALYTICS_EVENTS.PLACE_SELECT, {
      experience_type: experienceType,
      place_id: placeId,
      selection_source: selectionSource,
    });
  }, [experienceType, startExperience]);

  return {
    sectionRef,
    startExperience,
    trackPlaceSelect,
  };
}
