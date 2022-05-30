package io.github.xpakx.ladder.filter;

import io.github.xpakx.ladder.notification.NotifyOnFilterChange;
import io.github.xpakx.ladder.notification.NotifyOnFilterDeletion;
import io.github.xpakx.ladder.common.dto.BooleanRequest;
import io.github.xpakx.ladder.filter.dto.FilterRequest;
import io.github.xpakx.ladder.common.error.NotFoundException;
import io.github.xpakx.ladder.user.UserAccountRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class FilterService {
    private final FilterRepository filterRepository;
    private final UserAccountRepository userRepository;

    /**
     * Add new filter.
     * @param request Request with data to build new filter
     * @param userId ID of an owner of the newly created filter
     * @return Newly created filter
     */
    @NotifyOnFilterChange
    public Filter addFilter(FilterRequest request, Integer userId) {
        Filter filterToAdd = buildFilterToAddFromRequest(request, userId);
        filterToAdd.setGeneralOrder(filterRepository.getMaxOrderByOwnerId(userId)+1);
        return filterRepository.save(filterToAdd);
    }

    private Filter buildFilterToAddFromRequest(FilterRequest request, Integer userId) {
        return Filter.builder()
                .name(request.getName())
                .owner(userRepository.getById(userId))
                .color(request.getColor())
                .favorite(request.isFavorite())
                .searchString(request.getSearchString())
                .modifiedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Updating filter in repository.
     * @param request Data to update the filter
     * @param filterId ID of the filter to update
     * @param userId ID of an owner of the filter
     * @return Filter with updated data
     */
    @NotifyOnFilterChange
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

    /**
     * Delete filter from repository.
     * @param filterId ID of the label to delete
     * @param userId ID of an owner of the label
     */
    @NotifyOnFilterDeletion
    public void deleteFilter(Integer filterId, Integer userId) {
        filterRepository.deleteByIdAndOwnerId(filterId, userId);
    }

    /**
     * Change if filter is favorite.
     * @param request Request with favorite flag
     * @param filterId ID of the filter to update
     * @param userId ID of an owner of the filter
     * @return Updated filter
     */
    @NotifyOnFilterChange
    public Filter updateFilterFav(BooleanRequest request, Integer filterId, Integer userId) {
        Filter filterToUpdate = filterRepository.findByIdAndOwnerId(filterId, userId)
                .orElseThrow(() -> new NotFoundException("No such filter"));
        filterToUpdate.setFavorite(request.isFlag());
        filterToUpdate.setModifiedAt(LocalDateTime.now());
        return filterRepository.save(filterToUpdate);
    }
}
