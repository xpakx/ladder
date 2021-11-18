package io.github.xpakx.ladder.service;

import io.github.xpakx.ladder.entity.Filter;
import io.github.xpakx.ladder.entity.dto.FilterRequest;
import io.github.xpakx.ladder.repository.FilterRepository;
import io.github.xpakx.ladder.repository.UserAccountRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class FilterService {
    private final FilterRepository filterRepository;
    private final UserAccountRepository userRepository;

    public Filter addFilter(FilterRequest request, Integer userId) {
        Filter filterToAdd = buildFilterToAddFromRequest(request, userId);
        filterToAdd.setGeneralOrder(filterRepository.getMaxOrderByOwnerId(userId)+1);
        return filterRepository.save(filterToAdd);
    }

    private Filter buildFilterToAddFromRequest(FilterRequest request, Integer userId) {
        return Filter.builder()
                .name(request.getName())
                .color(request.getColor())
                .favorite(request.isFavorite())
                .searchString(request.getSearchString())
                .modifiedAt(LocalDateTime.now())
                .build();
    }
}
