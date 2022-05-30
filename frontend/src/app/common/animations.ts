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
    ])
}
