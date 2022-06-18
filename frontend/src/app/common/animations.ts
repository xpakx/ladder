import { animate, style, transition, trigger, group, query } from '@angular/animations';

export const Animations = {
    collapseTrigger: trigger('collapse', [
        transition(':enter', [
            style({ height: '0' }),
            group([
            animate('.375s', style({ height: '*' })),
            query('.projects_list_container',[
                style({ transform: 'translateY(-100%)' }),
                animate('.375s', style({ transform: 'translateY(0)' }))
            ])
            ])
        ]),
        transition(':leave', [
            style({ overflow: 'hidden' }),
            group([
            animate('.375s', style({ height: '0' })),
            query('.projects_list_container',[
                style({ transform: 'translateY(0)' }),
                animate('.375s', style({ transform: 'translateY(-100%)' }))
            ])
            ])
        ])
    ]),

    menuTrigger: trigger('menu', [
        transition(':enter', [
            style({ width: '0' }),
            group([
            animate('.25s', style({ width: '*' })),
            query('.left-nav',[
                style({ transform: 'translateX(-110%)' }),
                animate('.25s', style({ transform: 'translateX(0)' }))
            ])
            ])
        ]),
        transition(':leave', [
            style({ overflow: 'hidden' }),
            group([
            animate('.25s', style({ width: '0' })),
            query('.left-nav',[
                style({ transform: 'translateX(0)' }),
                animate('.25s', style({ transform: 'translateX(-110%)' }))
            ])
            ])
        ])
    ])
}
