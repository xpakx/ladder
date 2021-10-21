export interface Page<T> {
    content: T[];
    pageable: {
        sort: {
            sorted: boolean,
            unsorted: boolean,
            empty: boolean
        },
        offset:number,
        pageNumber: number,
        pageSize: number,
        paged: true,
        unpaged: true
    },
    totalPages: number,
    last: boolean,
    number: number,
    sort: {
        sorted: boolean,
        unsorted: boolean,
        empty: boolean
    },
    size: number,
    numberOfElements: number,
    first: boolean,
    empty: boolean
}