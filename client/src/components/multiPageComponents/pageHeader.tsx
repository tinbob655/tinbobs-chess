import React from 'react';


interface params {
    title: string,
    subtitle: string,
}

export default function PageHeader({title, subtitle}:params):React.ReactElement {

    return (
        <React.Fragment>
            <h1 className="alignLeft" style={{marginLeft: '10%'}}>
                {title}
            </h1>
            <p className="alignLeft" style={{marginLeft: '12.5%'}}>
                {subtitle}
            </p>
            <div className="dividerLine"></div>
        </React.Fragment>
    )
}