package io.github.xpakx.ladder.service;

import io.github.xpakx.ladder.entity.Filter;
import io.github.xpakx.ladder.entity.dto.FilterRequest;
import io.github.xpakx.ladder.error.NotFoundException;
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

    public Filter updateFilter(FilterRequest request, Integer userId, Integer filterId) {
        Filter filterToUpdate = filterRepository.findByIdAndOwnerId(filterId, userId)
                .orElseThrow(() -> new NotFoundException("No such filter!"));
        filterToUpdate.setName(request.getName());
        filterToUpdate.setColor(request.getColor());
        filterToUpdate.setFavorite(request.isFavorite());
        filterToUpdate.setSearchString(request.getSearchString());
        filterToUpdate.setModifiedAt(LocalDateTime.now());
        return filterRepository.save(filterToUpdate);
    }

    public void deleteFilter(Integer filterId, Integer userId) {
        filterRepository.deleteByIdAndOwnerId(filterId, userId);
    }
}
