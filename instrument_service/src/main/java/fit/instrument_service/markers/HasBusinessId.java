/*
 * @ {#} HasBusinessId.java   1.0     10/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.instrument_service.markers;

/*
 * @description: Marker interface for entities with business IDs
 * @author: Tran Hien Vinh
 * @date:   10/11/2025
 * @version:    1.0
 */
public interface HasBusinessId {
    void assignBusinessId(); // để mỗi entity tự định nghĩa cách set ID của mình
}
