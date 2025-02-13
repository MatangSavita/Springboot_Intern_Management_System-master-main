package com.rh4.services;


import com.rh4.entities.Feedback;
import com.rh4.repositories.FeedbackRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeedBackService {

        @Autowired
        private FeedbackRepo feedbackRepo;
//
        public List<Feedback> findAll() {
                return feedbackRepo.findAll();
        }

        public Feedback saveFeedback(Feedback feedback) {

                return feedbackRepo.save(feedback);
        }


        public List<Feedback> getFeedback() {
                return feedbackRepo.findAll();
        }


}
